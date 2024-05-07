procedure test_1 is
    n: integer;
    c: character;
    v: array(1..4) of integer;
----------------------------------------------------------
procedure f (r: ref array(1..4) of integer) is
begin
    v(2) := 4;
end;
----------------------------------------------------------
begin
    null;
    --put(v(n));
    --v(n) := 3;
    --put_line;
    f(v);
    --v(4) := 2;
end;
