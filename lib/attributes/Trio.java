package lib.attributes;

import lib.symbolTable.Symbol;

public class Trio {
    public Symbol.Types tipo;
    public boolean referencia;
    public String nombre;

    public Trio() {
        this.tipo = Symbol.Types.UNDEFINED;
        this.referencia = false;
        this.nombre = "";
    }
}
