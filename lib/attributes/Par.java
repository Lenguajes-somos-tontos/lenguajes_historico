package lib.attributes;

import lib.symbolTable.Symbol;

public class Par {
    public Symbol.Types primero;
    public boolean segundo;

    public Par(Symbol.Types primero, boolean segundo) {
        this.primero = primero;
        this.segundo = segundo;
    }
}
