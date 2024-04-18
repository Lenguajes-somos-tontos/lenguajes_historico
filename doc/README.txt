Compilador alike.jar (V1.0)
------------------------------
Análisis léxico, sintáctico y semántico


Invocar como:

-------------------------------------------------------------
java -jar alike.jar <fichero_fuente_clike>
-------------------------------------------------------------

Si se invoca sin parámetros, lee desde la entrada estándar.


Características generales:

- Declaraciones: Producción que se dispara en cada línea de declaracion de variables.
  Cada vez que se llama se recogen todos los IDs leídos en un ArrayList sin verificar
  que sean correctos. Una función auxiliar recibe el array con los nombres y un objeto Symbol
  que hace de "plantilla" para insertar los símbolos en la tabla de símbolos.
  Ahí es cuando se verifican los posibles errores (símbolo ya definido o índices no correctos
  en caso de que el símbolo sea un array).

- Procedimientos/Funciones: Tienen una estructura muy similar. Antes de leer la lista de parámetros,
  que puede ser nula, se inserta el propio símbolo del procedimiento/función (en caso de que se pueda),
  después se inserta el nuevo bloque para parámetros y variables locales. Posteriormente se leen los
  parámetros (siempre y cuando haya), los cuales siguen una lógica similar a las declaraciones de variables locales.
  "Parametros()" crea un array de símbolos, que serán los parámetros y los inserta en la tabla de símbolos.
  Este array es rellenado por "Parametro()", que reutiliza la regla "Lista_IDS()" y utiliza "Parametro_aux()"
  para nuevamente definir una plantilla de símbolos, en caso de que haya varios símbolos del mismo tipo seguidos.

- Expresiones: De estas se guarda lo necesario para los diferentes casos de instrucciones (llamadas a
  funciones/procedimientos y asignaciones). Se guarda: el tipo de dato de la expresión (UNDEFINED en caso de error),
  un bool que indica si la expresión es un asignable (únicamente una variable o componente de array sin ningún operando),
  y el nombre de la variable, en caso de que sea un asignable (caso argumento array en llamadas a procedimientos/funciones).
  Cuando se lee únicamente un ID se verifica que sea una variable, o bien una función, en cualquier otro caso es un error.
  Si por el contrario es una llamada a un procedimiento/función o una componente de un array, se lee la lista de argumentos
  introducidos, y mediante métodos intermedios, se deshace la ambigüedad y se devuelve el correspondiente tipo de dato.

- Return: El que la instrucción especial "return" solamente sea válida dentro del cuerpo de una función,
  se verifica a nivel sintáctico. Para ello se emplean dos "sets" de instrucciones distintos, en el que en uno de ellos
  no se contempla la instrucción return, lo que implica que las producciones de instrucciones iterativas y condicionales
  estén replicadas. Del return se verifica que el tipo de dato que devuelve corresponda al tipo
  que devuelve la función, así como los flujos de instrucción, para que en caso de que sea posible que una función
  no alcance una instrucción return, salte el correspondiente aviso.

- Asignación: Únicamente se comprueba que el símbolo a asignar exista (en caso de estar asignando una componente de
  un array, se verifica que el ID corresponda a un array y la expresión para calcular el índice sea de tipo INT), y que
  el tipo de la expresión corresponda al tipo del símbolo que se está asignando (en el caso del array, a su tipo base).

- Llamadas a funciones/procedimientos: Siguen la misma lógica, salvo que la llamada a una función sin asignación es considerado
  error semántico. Se lee la lista de argumentos que introduce el usuario (clase Trio que devuelve "Expresion()")
  y se compara con la lista de parámetros del procedimiento/función invocado. En el caso de los procedimientos/funciones
  especiales (int2char, char2int, skip_line, get, put, put_line), los que se conocen a priori sus parámetros
  (int2char, char2int, skip_line) se introducen al principio del análisis para tratarlos como casos normales que serán
  reconocios como métodos escritos por el usuario. En cambio, "get", "put" y "put_line" son tratados como casos especiales,
  ya que no se puede conocer su número de parámetros, por lo que en las comprobaciones de las invocaciones,
  se comprueba antes que el ID invocado sea uno de ellos (para que no de error de símbolo no definido).
