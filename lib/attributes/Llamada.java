package lib.attributes;

import java.util.ArrayList;

import lib.symbolTable.Symbol;

public class Llamada {
    public Symbol simbolo;
    public ArrayList<Symbol> lista;

    public Llamada() {
        simbolo = null;
        lista = null;
    }
}
