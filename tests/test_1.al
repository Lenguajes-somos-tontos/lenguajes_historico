procedure test_1 is
    n: integer;
    c: character;
    v: array(0..2) of integer;
----------------------------------------------------------
procedure f2 (y: integer) is
    si: integer;
begin
    null;
end;
----------------------------------------------------------
procedure f1 (x: ref integer) is
    si: integer;
begin
    --x(3) := x(2);
    --put(x);
    --f2(x);
    x := 1;
    null;
end;
----------------------------------------------------------
begin
    null;
    --put(v(n));
    --v(n) := 3;
    --put_line;
    f1(n);
    --f2(v);
    --v(4) := 2;
end;
