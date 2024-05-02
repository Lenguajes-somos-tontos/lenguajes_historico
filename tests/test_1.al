procedure test_1 is
	n: integer;
    k: boolean;
    c: character;
    v: ARRAY(4..1000) OF CHARACTER;
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
    k := true and (false or k);
end;
