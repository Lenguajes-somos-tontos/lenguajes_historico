procedure test_1 is
    n: integer;
    c: character;
    v: array(0..2) of integer;
----------------------------------------------------------
procedure f2 (y: array(0..2) of integer) is
    si: integer;
begin
    null;
end;
----------------------------------------------------------
procedure f1 (x: array(0..2) of integer) is
    si: integer;
begin
    --x(3) := x(2);
    --put(x);
    f2(x);
    --x := si;
    null;
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
