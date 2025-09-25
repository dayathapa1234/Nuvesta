package com.nuvesta.market_data_service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nuvesta.market_data_service.model.DailyPrice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class YahooFinanceClient {

    private static final Logger log = LoggerFactory.getLogger(YahooFinanceClient.class);

    private final HttpClient http = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    private final ObjectMapper mapper = new ObjectMapper();

    /** Cache resolved aliases to avoid re-searching every run. */
    private final Map<String, String> aliasCache = new ConcurrentHashMap<>();

    /**
     * Fetch daily OHLCV between start and end (inclusive).
     * - Caps end at today (UTC)
     * - No-ops if start > end after capping
     * - Tries normalized symbol variants before search
     * - Treats empty windows and delisted symbols as normal skips (DEBUG)
     */
    public List<DailyPrice> fetchHistory(String rawSymbol, LocalDate start, LocalDate end) {
        try {
            if (rawSymbol == null || rawSymbol.isBlank()) {
                if (log.isDebugEnabled()) log.debug("Empty symbol");
                return List.of();
            }

            // ---- clamp end to today-UTC without mutating the captured vars ----
            LocalDate todayUtc = LocalDate.now(ZoneOffset.UTC);
            LocalDate adjEnd = end.isAfter(todayUtc) ? todayUtc : end;
            if (adjEnd.isBefore(start)) {
                if (log.isDebugEnabled()) log.debug("No new window for {}: start {} > end {}", rawSymbol, start, adjEnd);
                return List.of();
            }

            // widen request a bit to survive weekends/holidays; we'll filter back to [start, adjEnd]
            LocalDate reqStart = start.minusDays(7);
            if (reqStart.isAfter(adjEnd)) reqStart = start;

            long p1 = reqStart.atStartOfDay(ZoneId.of("UTC")).toEpochSecond();
            long p2 = adjEnd.plusDays(1).atStartOfDay(ZoneId.of("UTC")).toEpochSecond(); // inclusive

            // >>> make effectively-final copies for the lambda <<<
            final LocalDate from = start;
            final LocalDate to   = adjEnd;

            // NEW: track which symbols we've already attempted in THIS call
            Set<String> attempted = new HashSet<>();

            // helper that avoids duplicate calls and captures only final/effectively-final vars
            java.util.function.Function<String, List<DailyPrice>> tryOnce = (sym) -> {
                String key = sym.toUpperCase(Locale.ROOT);
                if (!attempted.add(key)) {
                    if (log.isDebugEnabled()) log.debug("Already tried {} in this fetch, skipping duplicate", sym);
                    return List.of();
                }
                return filterWindow(tryChartWithRetry(sym, p1, p2), from, to);
            };

            // 0) cached alias
            String cached = aliasCache.get(rawSymbol);
            if (cached != null) {
                List<DailyPrice> rows = tryOnce.apply(cached);
                if (!rows.isEmpty()) return normalizeSymbol(rows, cached);
            }

            // 1) local variants
            for (String sym : buildVariants(rawSymbol)) {
                List<DailyPrice> rows = tryOnce.apply(sym);
                if (!rows.isEmpty()) {
                    aliasCache.put(rawSymbol, sym);
                    return normalizeSymbol(rows, sym);
                }
            }

            // 2) resolver
            Optional<String> resolved = resolveSymbol(rawSymbol);
            if (resolved.isPresent()) {
                String ySym = resolved.get();
                if (!ySym.equalsIgnoreCase(rawSymbol)) {
                    log.info("Resolved Yahoo symbol {} -> {}", rawSymbol, ySym);
                }
                aliasCache.put(rawSymbol, ySym);
                List<DailyPrice> rows = tryOnce.apply(ySym);   // will skip if already attempted
                if (!rows.isEmpty()) return normalizeSymbol(rows, ySym);
            }

            if (log.isDebugEnabled()) log.debug("No data for symbol {} (after resolve attempt). Skipping.", rawSymbol);
            return List.of();

        } catch (Exception e) {
            log.warn("Failed to fetch Yahoo history for {}: {}", rawSymbol, e.toString());
            return List.of();
        }
    }


    private List<String> buildVariants(String raw) {
        String s = raw.trim().toUpperCase(Locale.ROOT);

        String cleaned = s.replace(' ', '-')
                .replace('/', '-')
                .replaceAll("\\.([A-Z])$", "-$1"); // BRK.B -> BRK-B

        Set<String> variants = new LinkedHashSet<>();
        variants.add(s);
        variants.add(cleaned);

        if (cleaned.matches(".*-U$")) {
            variants.add(cleaned.substring(0, cleaned.length() - 2) + "-UN");
        }
        if (cleaned.matches(".*-W$")) {
            variants.add(cleaned.substring(0, cleaned.length() - 2) + "-WT");
        }
        if (s.matches(".*\\.U$")) variants.add(s.substring(0, s.length() - 2) + "-UN");
        if (s.matches(".*\\.W$")) variants.add(s.substring(0, s.length() - 2) + "-WT");

        return new ArrayList<>(variants);
    }

    /** Build the exact chart URL (used for logging + requests). */
    private String buildChartUrl(String symbol, long period1, long period2) {
        return String.format(
                "https://query2.finance.yahoo.com/v8/finance/chart/%s?period1=%d&period2=%d&interval=1d&events=div%%2Csplit",
                URLEncoder.encode(symbol, StandardCharsets.UTF_8),
                period1, period2
        );
    }

    private List<DailyPrice> tryChart(String symbol, long period1, long period2) {
        try {
            String url = buildChartUrl(symbol, period1, period2);

            // 🔎 Print the exact URL being called
            log.info("GET {}", url);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0")
                    .header("Accept", "application/json")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .build();

            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() == 400 && resp.body() != null
                    && resp.body().contains("Data doesn't exist for startDate")) {
                if (log.isDebugEnabled()) log.debug("Empty window for {}: {}", symbol, resp.body());
                return List.of();
            }

            if (resp.statusCode() == 404) {
                if (log.isDebugEnabled()) log.debug("Not found (delisted?) for {}: {}", symbol,
                        resp.body() == null ? "" : resp.body());
                return List.of();
            }

            if (resp.statusCode() != 200) {
                String body = resp.body();
                log.warn("Yahoo chart API {} returned {}: {}",
                        symbol, resp.statusCode(),
                        body == null ? "" : body.substring(0, Math.min(240, body.length()))
                );
                return List.of();
            }
            return parseChartJson(symbol, resp.body());

        } catch (Exception e) {
            log.warn("Chart call failed for {}: {}", symbol, e.toString());
            return List.of();
        }
    }

    private List<DailyPrice> tryChartWithRetry(String symbol, long period1, long period2) {
        final int maxAttempts = 4;
        long backoff = 800L; // ms
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            List<DailyPrice> rows = tryChart(symbol, period1, period2);
            if (!rows.isEmpty()) return rows;

            if (attempt < maxAttempts) {
                try { Thread.sleep(backoff); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                backoff *= 2;
            }
        }
        return List.of();
    }

    private Optional<String> resolveSymbol(String raw) {
        try {
            String url = "https://query2.finance.yahoo.com/v1/finance/search?q="
                    + URLEncoder.encode(raw, StandardCharsets.UTF_8)
                    + "&quotesCount=5&newsCount=0";

            // 🔎 Only DEBUG to avoid noise
            if (log.isDebugEnabled()) log.debug("SEARCH {}", url);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0")
                    .header("Accept", "application/json")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200 || resp.body() == null || resp.body().isBlank()) {
                return Optional.empty();
            }

            JsonNode quotes = mapper.readTree(resp.body()).path("quotes");
            if (!quotes.isArray() || quotes.isEmpty()) return Optional.empty();

            // Prefer EQUITY/ETF/MUTUALFUND; otherwise first available symbol
            for (JsonNode q : quotes) {
                String sym = q.path("symbol").asText("");
                String qt  = q.path("quoteType").asText("");
                if (!sym.isBlank() &&
                        ("EQUITY".equalsIgnoreCase(qt) ||
                                "ETF".equalsIgnoreCase(qt) ||
                                "MUTUALFUND".equalsIgnoreCase(qt))) {
                    return Optional.of(sym);
                }
            }
            String sym = quotes.get(0).path("symbol").asText("");
            return sym.isBlank() ? Optional.empty() : Optional.of(sym);

        } catch (Exception ignore) {
            return Optional.empty();
        }
    }

    /** Parse chart JSON to DailyPrice rows (null-safe for sparse arrays). */
    private List<DailyPrice> parseChartJson(String symbol, String json) throws Exception {
        List<DailyPrice> out = new ArrayList<>();
        JsonNode root = mapper.readTree(json).path("chart");

        JsonNode err = root.path("error");
        if (!err.isNull() && !err.isMissingNode()) {
            String code = err.path("code").asText("");
            String desc = err.path("description").asText("");
            if (log.isDebugEnabled()) log.debug("Yahoo error for {}: {} - {}", symbol, code, desc);
            return out;
        }

        JsonNode results = root.path("result");
        if (!results.isArray() || results.isEmpty()) return out;

        JsonNode r0 = results.get(0);
        JsonNode timestamps = r0.path("timestamp");
        if (!timestamps.isArray() || timestamps.isEmpty()) return out;

        JsonNode quote0 = r0.path("indicators").path("quote");
        if (!quote0.isArray() || quote0.isEmpty()) return out;

        JsonNode q = quote0.get(0);
        JsonNode opens  = q.path("open");
        JsonNode highs  = q.path("high");
        JsonNode lows   = q.path("low");
        JsonNode closes = q.path("close");
        JsonNode vols   = q.path("volume");

        for (int i = 0; i < timestamps.size(); i++) {
            long ts = timestamps.get(i).asLong();
            LocalDate date = Instant.ofEpochSecond(ts).atZone(ZoneId.of("UTC")).toLocalDate();

            BigDecimal open  = getBD(opens,  i);
            BigDecimal high  = getBD(highs,  i);
            BigDecimal low   = getBD(lows,   i);
            BigDecimal close = getBD(closes, i);
            long volume      = getLong(vols, i);

            if (open == null && close == null) continue; // skip non-trading days

            out.add(new DailyPrice(
                    null,
                    symbol.toUpperCase(),
                    date,
                    open, high, low, close,
                    volume
            ));
        }
        return out;
    }

    /** Keep only rows within [start, end]. */
    private static List<DailyPrice> filterWindow(List<DailyPrice> rows, LocalDate start, LocalDate end) {
        if (rows == null || rows.isEmpty()) return List.of();
        List<DailyPrice> out = new ArrayList<>(rows.size());
        for (DailyPrice dp : rows) {
            LocalDate d = dp.getDate();
            if ((d.isAfter(start) || d.isEqual(start)) && (d.isBefore(end) || d.isEqual(end))) {
                out.add(dp);
            }
        }
        return out;
    }

    private static List<DailyPrice> normalizeSymbol(List<DailyPrice> rows, String sym) {
        if (rows == null || rows.isEmpty()) return rows;
        String upper = sym.toUpperCase(Locale.ROOT);
        rows.replaceAll(dp -> new DailyPrice(
                dp.getId(), upper, dp.getDate(),
                dp.getOpen(), dp.getHigh(), dp.getLow(), dp.getClose(), dp.getVolume()
        ));
        return rows;
    }

    private static BigDecimal getBD(JsonNode arr, int i) {
        if (arr == null || !arr.isArray() || i >= arr.size()) return null;
        JsonNode n = arr.get(i);
        if (n == null || n.isNull() || !n.isNumber()) return null;
        return n.decimalValue();
    }

    private static long getLong(JsonNode arr, int i) {
        if (arr == null || !arr.isArray() || i >= arr.size()) return 0L;
        JsonNode n = arr.get(i);
        if (n == null || n.isNull() || !n.isNumber()) return 0L;
        return n.asLong();
    }
}
