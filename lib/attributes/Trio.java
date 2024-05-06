package lib.attributes;

import lib.symbolTable.Symbol;

public class Trio {
    public Symbol.Types tipo;
    public boolean referencia;
    public Symbol simbolo;
    public String nombre;

    public Trio() {
        this.tipo = Symbol.Types.UNDEFINED;
        this.referencia = false;
        this.simbolo = null;
        nombre = "";
    }

    public String toString() {
        return tipo +  " " + Boolean.toString(referencia) + " " + simbolo.name;
    }
}
