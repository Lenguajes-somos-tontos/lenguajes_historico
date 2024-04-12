procedure test_3 is
	cuenta, n, num: integer;
    v: array(1..1000) of integer;
------------------------------------------------------
------------------------------------------------------
procedure calcular(n, num: integer; v: ref array(1..1000) of integer) is
    i: integer;
    cuenta: integer;
begin
    i := 1;
    cuenta := 0;
    while i < n loop
        if v(siguiente(i)) = num then
            cuenta := cuenta + 1;
        end if;
        i := i + 1;
    end loop;
    return cuenta;
end;
------------------------------------------------------
------------------------------------------------------
function siguiente(n: integer) return integer is
begin
	if n < 0 then
		return -1;
	else
        return n;
	end if;
end;
------------------------------------------------------
------------------------------------------------------
procedure inicializar(n: integer; v: ref array(1..1000) of integer) is
    i: integer;
begin
    i := 1;
    while i < n loop
        v(siguiente(i)) := siguiente(0);
        i := i + 1;
    end loop;

end;
------------------------------------------------------
------------------------------------------------------
procedure randomizar(n: integer; v: ref array(1..1000) of integer) is
    i: integer;
begin
    i := 1;
    while i < n loop
        v(siguiente(i)) := rand(siguiente(i), siguiente(n));
        i := i + 1;
    end loop;
end;
------------------------------------------------------
------------------------------------------------------
begin
    cuenta := 0;
    n := 1000;
    num := rand(siguiente(0), siguiente(n));
    inicializar(n, v);
    randomizar(n, v);

    cuenta := calcular(n, num ,v);
    put_line("El numero ", num, " aparece en el array ", cuenta, " veces");
end;
