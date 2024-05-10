procedure test_1 is
    --n: integer;
    --c: character;
    v: array(1..1) of integer;
----------------------------------------------------------
procedure f1 (r: ref array(1..1) of integer) is
begin
    put("s");
end;
----------------------------------------------------------
procedure f2 (r: ref array(1..1) of integer) is
begin
    put("s");
end;
----------------------------------------------------------
begin
    null;
    --put(v(n));
    --v(n) := 3;
    --put_line;
    f1(v);
    f2(v);
    --v(4) := 2;
end;
