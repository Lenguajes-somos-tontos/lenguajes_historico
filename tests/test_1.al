procedure test_1 is
    n: integer;
    --c: character;
    v: array(1..4) of integer;
----------------------------------------------------------
procedure f1 (r: ref array(1..4) of integer) is
begin
    r(2) := 3;
end;
----------------------------------------------------------
begin
    null;
    --put(v(n));
    --v(n) := 3;
    --put_line;
    f1(v);
    --f2(v);
    --v(4) := 2;
end;
