procedure test_1 is
	n: integer;
    k: boolean;
    c: character;
    v: ARRAY(-134534..-1000) OF CHARACTER;
    t: ARRAY(1..1000) OF boolean;
------------------------------------------------------
function inicializar(n: ref boolean; vi: ref array(1..1000) of boolean; i: CHARACTER) return boolean is
    otra: integer;
begin
    if true then
        return true;
    end if;
    return true;
end;
----------------------------------------------------------
begin
    put("Hola", 5, true, false, 'c');
    k := inicializar(t(n+1), t, v(n*21+45));
    c :=int2char(45);
    n := char2int('c');
    put_line;
    get(n,c);
    v(n) := 'c';
    null;
end;
