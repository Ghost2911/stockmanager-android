package com.ghost2911.stockmanager;

public class Product {
    int idProduct;
    int idRoot;
    String name;
    String desc;
    int price;
    int count;
    String analog;
    String barcode;
    String storage;

    Product(int _idProduct, int _idRoot, String _name, String _desc, int _price, int _count, String _analog, String _barcode, String _storage) {
        idProduct = _idProduct;
        idRoot = _idRoot;
        name = _name;
        desc = _desc;
        price = _price;
        count = _count;
        analog = _analog;
        barcode =  _barcode;
        storage = _storage;
    }
}
