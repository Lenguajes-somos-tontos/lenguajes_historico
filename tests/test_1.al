procedure test_1 is
	n: integer;
    k: boolean;
    c: character;
    v: ARRAY(1..1000) OF CHARACTER;
------------------------------------------------------
function inicializar(n: boolean; vi: ref array(1..1000) of CHARACTER; i: CHARACTER) return boolean is
    otra: integer;
begin
    return false;
end;
----------------------------------------------------------
function prueba_sin return boolean is
    otra: integer;
begin
    return true;
end;
----------------------------------------------------------
procedure calcular(n, num: integer; v: ref array(1..1000) of CHARACTER) is
    i: integer;
    cuenta: integer;
begin
    null;
end;
----------------------------------------------------------
begin
    --k := inicializar(prueba_sin, v, v(n*21+45));
    calcular(n, n+2*5, v);
    put("Hola", 5, true, false, 'c');
    c :=int2char(45);
    n := char2int('c');
    skip_line;
    k := prueba_sin;
end;
