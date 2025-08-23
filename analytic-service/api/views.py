from rest_framework.viewsets import ModelViewSet
from .models import Item
from .serializers import ItemSerializer

class ItemViewSet(ModelViewSet):
    queryset = Item.objects.all().order_by("-id")
    serializer_class = ItemSerializer