package lib.attributes;

import lib.symbolTable.Symbol;

public class Trio {
    public Symbol.Types primero;
    public boolean segundo;
    public String tercero;

    public Trio() {
        this.primero = Symbol.Types.UNDEFINED;
        this.segundo = false;
        this.tercero = "";
    }
}
