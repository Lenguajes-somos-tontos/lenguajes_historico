PROCEDURE test_2 is
	n, i: INTEGER;
    v: ARRAY(1..1000) OF integer;
------------------------------------------------------
function inicializar(n, j: integer; vi: array(1..777) of boolean) return boolean is
    i: integer;
begin
    I := 1;
    while i < n loop
        v(siguiente(i)) := siguiente(0);
        i := i + 1;
    end loop;

end;
------------------------------------------------------
begin
    n := 1000;
    INICIALIZAR(n, v);
    --randomizar(n, v);

    put_line("La media de esta distribucion es: ", media(n, v), " y la moda: ", moda(n, v));
end;
