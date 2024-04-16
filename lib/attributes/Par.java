package lib.attributes;

import lib.symbolTable.Symbol;

public class Par {
    public Symbol.Types primero;
    public boolean segundo;
    public String tercero;

    public Par() {
        this.primero = Symbol.Types.UNDEFINED;
        this.segundo = false;
        this.tercero = "";
    }
}
