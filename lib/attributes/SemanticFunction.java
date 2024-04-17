//*****************************************************************
// File:   SemanticFunction.java
// Author: Jorge Rodilla Esteve 845389 Enrique Martinez Casanova 839773
// Date:   marzo 2024
//*****************************************************************

package lib.attributes;

import lib.symbolTable.*;
import lib.symbolTable.exceptions.*;
import java.util.*;
import lib.attributes.*;
import traductor.*;

public class SemanticFunction {

    public void Declaracion(ArrayList<String> array_nombres_variables, Symbol tipo_variables, SymbolTable st) {
        // Se tiene un simbolo sin nombre y un array de Strings
		for (String variable : array_nombres_variables) {
			tipo_variables.name = variable;					// Se asigna el nombre
			try {st.insertSymbol(tipo_variables.clone());}	// Se inserta una copia del puntero
			catch (AlreadyDefinedSymbolException e) {
				simbolo_definido(variable);
				alike.codigo_error();
			}
			catch (IndexArrayNotCorrect i) {	// Caso de simbolo array con indices incorrectos
				System.out.println("Los indices del array " + variable + " no son correctos");
				alike.codigo_error();
			}
		}
    }


    public Symbol Tipo_variable(Token t, Symbol result) {
        switch (t.image.toLowerCase()) {
			case "boolean":
				result = new SymbolBool("");
				break;
			case "integer":
				result = new SymbolInt("");
				break;
			case "character":
				result = new SymbolChar("");
				break;
		}
		return result;
    }


    public Symbol Tipo_array(Token indMin, Token indMax, Symbol tipo_base) {
		int indMin_i = Integer.parseInt(indMin.image);
		int indMax_i = Integer.parseInt(indMax.image);
		Symbol result = new SymbolArray("", indMin_i, indMax_i, tipo_base.type);
		return result;
    }


    public void Primario_ID(Token t, Par tipo, SymbolTable st) {
    	try {
			Symbol simbolo = st.getSymbol(t.image);

			if (simbolo.type == Symbol.Types.INT) {
				tipo.primero = Symbol.Types.INT;
				tipo.segundo = true;
				tipo.tercero = t.image;
			}
			else if (simbolo.type == Symbol.Types.BOOL) {
				tipo.primero = Symbol.Types.BOOL;
				tipo.segundo = true;
				tipo.tercero = t.image;
			}
			else if (simbolo.type == Symbol.Types.CHAR) {
				tipo.primero = Symbol.Types.CHAR;
				tipo.segundo = true;
				tipo.tercero = t.image;
			}
			else if (simbolo.type == Symbol.Types.ARRAY) {
				tipo.primero = Symbol.Types.ARRAY;
				tipo.segundo = true;
				tipo.tercero = t.image;
			}
			else if (simbolo.type == Symbol.Types.FUNCTION) {
				SymbolFunction s = (SymbolFunction) simbolo;
				if (s.parList.isEmpty()) {	// Se verifica que la función no tiene parámetros
					tipo.primero = s.returnType;
					// Se sube el tipo que retorna la función
				}
				else {
					System.out.println("ERROR: Los parámetros de la llamada a " + t.image + " no son correctos");
					alike.codigo_error();
					// Se ha utilizado una función que tiene parámetros como una que no los tiene
				}
			}
		}
		catch (SymbolNotFoundException s) {
			simbolo_no_definido(t.image);
		}
    }


	public void Asignacion(String id, Par tipo_asignacion, SymbolTable st) {
		try {
			Symbol simbolo_asignacion = st.getSymbol(id);
			Symbol.Types tipo_id = simbolo_asignacion.type;

			if (tipo_id != tipo_asignacion.primero || tipo_id == Symbol.Types.FUNCTION
				|| tipo_id == Symbol.Types.PROCEDURE || tipo_id == Symbol.Types.ARRAY || tipo_id == Symbol.Types.STRING) {
				esperaba_tipo(tipo_id);
				alike.codigo_error();
			}
		}
		catch (SymbolNotFoundException s) {
			simbolo_no_definido(id);
		}
	}


	public void Instr_funcion_vector_3(String id, Par tipo_primera_expresion, Par tipo_asignacion, SymbolTable st) {
		try {
			Symbol simbolo = st.getSymbol(id);
			SymbolArray simbolo_array = (SymbolArray) simbolo;

			if (tipo_primera_expresion.primero != Symbol.Types.INT) {
				esperaba_tipo(Symbol.Types.INT);
				alike.codigo_error();
			}
			else {
				if (tipo_asignacion.primero != simbolo_array.baseType) {
					// No se verifica que la expresión a la derecha pueda ser string, función, array o procedimiento
					// A nivel sintáctico, un array sólo puede ser de tipo INT, BOOL o CHAR
					esperaba_tipo(simbolo_array.baseType);
					alike.codigo_error();
				}
			}
		}
		catch (SymbolNotFoundException s) {
			simbolo_no_definido(id);
		}
		catch (ClassCastException e) {	// Excepción que saltará si se intenta meter un simbolo que no es un array en un SymbolArray
			simbolo_no_es(id, "array");
		}
	}


	public void comprobar_funciones_especiales(String id_funcion) throws SpecialFunctionFound {
		String id = id_funcion.toLowerCase();
		if (id.equals("put_line") || id.equals("put") || id.equals("get")) {
			throw new SpecialFunctionFound();
		}
	}


	public Par llamada_funcion(SymbolFunction simbolo_funcion, ArrayList<Par> lista_argumentos, SymbolTable st) {
		Par resultado = new Par();
		resultado.primero = simbolo_funcion.returnType;
		ArrayList<Symbol> lista_parametros = simbolo_funcion.parList;
		verificar_argumentos(lista_parametros, lista_argumentos, st);
		return resultado;
	}


	public Par indice_array(SymbolArray simbolo_array, Par indice) {
		Par resultado = new Par();

		// La expresión para calcular el índice no es de tipo INT
		if (indice.primero != Symbol.Types.INT) {
			esperaba_tipo(Symbol.Types.INT);
			alike.codigo_error();
		}
		else {
			resultado.primero = simbolo_array.baseType;
		}
		return resultado;
	}


	public Par verificar_expresion(String id, ArrayList<Par> lista_argumentos, SymbolTable st) {
		Par resultado = new Par();
		try {
			comprobar_funciones_especiales(id);

			Symbol simbolo = st.getSymbol(id);

			if (simbolo.type == Symbol.Types.FUNCTION) {
				SymbolFunction simbolo_funcion = (SymbolFunction) simbolo;
				resultado = llamada_funcion(simbolo_funcion, lista_argumentos, st);
			}
			else if (simbolo.type == Symbol.Types.ARRAY) {
				SymbolArray simbolo_array = (SymbolArray) simbolo;
				if (lista_argumentos.size() == 1) {
					resultado = indice_array(simbolo_array, lista_argumentos.get(0));
				}
				else {
					System.out.println("ERROR: Se esperaba un único índice en el array " + id);
					alike.codigo_error();
				}
			}
		}
		catch (SymbolNotFoundException s) {
			simbolo_no_definido(id);
		}
		// Se captura la excepción pero no saca error, para evitar el error de símbolo no definido,
		// el tipo que se va a devolver es UNDEFINED, que dará error en la llamada a la función que anida a esta otra llamada
		catch (SpecialFunctionFound g) {}
		return resultado;
	}


	public void verificar_procedimiento(SymbolProcedure simbolo_procedimiento, ArrayList<Par> lista_argumentos, SymbolTable st) {
		String id = simbolo_procedimiento.name;
		ArrayList<Symbol> lista_parametros = simbolo_procedimiento.parList;
		verificar_argumentos(lista_parametros, lista_argumentos, st);
	}


	public void verificar_argumentos(ArrayList<Symbol> lista_parametros, ArrayList<Par> lista_argumentos, SymbolTable st) {
		int numero_parametros = lista_parametros.size();
		if (numero_parametros == lista_argumentos.size()) {
			for (int i = 0; i < numero_parametros; i++) {

				Symbol parametro = lista_parametros.get(i);
				Par argumento = lista_argumentos.get(i);

				// Tipos no coinciden
				if (argumento.primero != parametro.type) {
					esperaba_tipo(parametro.type);
					alike.codigo_error();
				}

				// Tipo por referencia y no es asignable
				if (parametro.parClass == Symbol.ParameterClass.REF && !argumento.segundo) {
					System.out.println("ERROR: El parámetro " + parametro.name + " es un parámetro por referencia");
					alike.codigo_error();
				}

				// Caso ARRAY
				if (parametro.type == Symbol.Types.ARRAY) {
					SymbolArray array_argumento = (SymbolArray) (st.getSymbol(argumento.tercero));
					SymbolArray array_parametro = (SymbolArray) (parametro);

					// Índices no coinciden
					if (array_argumento.minInd != array_parametro.minInd || array_argumento.maxInd != array_parametro.maxInd) {
						System.out.println("ERROR: Los índices del array parámetro " + parametro.name + " no coinciden");
						alike.codigo_error();
					}

					// Tipos no coinciden
					if (array_argumento.baseType != array_parametro.baseType) {
						System.out.println("ERROR: Los tipos base del array parámetro " + parametro.name + " no coinciden");
						alike.codigo_error();
					}
				}
			}
		}
		else {
			System.out.println("ERROR: Se esperaban " + numero_parametros + " parametros");
			alike.codigo_error();
		}
	}


	public void llamada_procedimiento(String id, ArrayList<Par> lista_argumentos, SymbolTable st) {
		try {
			comprobar_funciones_especiales(id);
			Symbol simbolo = st.getSymbol(id);
			SymbolProcedure simbolo_procedimiento = (SymbolProcedure) simbolo;
			verificar_procedimiento(simbolo_procedimiento, lista_argumentos, st);
		}
		catch (SymbolNotFoundException s) {
			simbolo_no_definido(id);
		}
		catch (ClassCastException e) {
			simbolo_no_es(id, "procedimiento");
		}
		catch (SpecialFunctionFound g) {
			for (int i = 0; i < lista_argumentos.size(); i++) {
				Par argumento = lista_argumentos.get(i);
				if (id.equals("get") && (argumento.primero != Symbol.Types.INT && argumento.primero != Symbol.Types.CHAR)) {
					System.out.println("ERROR: Se esperaba un tipo INT/CHAR");
					alike.codigo_error();
				}
				else if ((id.equals("put") || id.equals("put_line")) && (argumento.primero != Symbol.Types.INT &&
					argumento.primero != Symbol.Types.BOOL && argumento.primero != Symbol.Types.CHAR && argumento.primero != Symbol.Types.STRING)) {
						System.out.println("ERROR: Se esperaba un tipo INT/BOOL/CHAR/STRING");
						alike.codigo_error();
				}
			}
		}
	}


	public void verificar_bool(Symbol.Types tipo) {
		if (tipo != Symbol.Types.BOOL) {
			esperaba_tipo(Symbol.Types.BOOL);
			alike.codigo_error();
		}
	}


	public void verificar_relacion(String operador, Par tipo, Par tipo2) {
		tipo.segundo = false;
		if (operador.equals("<") || operador.equals(">") || operador.equals("<=") || operador.equals(">=")) {
			if (tipo.primero != Symbol.Types.INT || tipo2.primero != Symbol.Types.INT) {
				tipo.primero = Symbol.Types.UNDEFINED;
				esperaba_tipo(Symbol.Types.INT);
				alike.codigo_error();
			}
			else tipo.primero = Symbol.Types.BOOL;
		}
		else if (tipo2.primero != Symbol.Types.ARRAY || tipo2.primero != Symbol.Types.FUNCTION ||
				tipo2.primero != Symbol.Types.PROCEDURE || tipo2.primero != Symbol.Types.STRING) {
			if (tipo.primero != tipo2.primero) {
				tipo.primero = Symbol.Types.UNDEFINED;
				esperaba_tipo(tipo.primero);
				alike.codigo_error();
			}
			else tipo.primero = Symbol.Types.BOOL;
		}
		else {
			System.out.println("ERROR: Se esperaba un tipo INT/BOOL/CHAR");
			alike.codigo_error();
		}
	}


	public void verificar_int(Par tipo, Par tipo2) {
		// Si se ha hecho matching con ()*, la expresión ya NO es sólo una variable
		tipo.segundo = false;
		if (tipo.primero != Symbol.Types.INT || tipo2.primero != Symbol.Types.INT) {
			esperaba_tipo(Symbol.Types.INT);
			tipo.primero = Symbol.Types.UNDEFINED;
			alike.codigo_error();
		}
	}


	public void simbolo_no_definido(String id) {
		System.out.println("ERROR: El simbolo " + id + " no está definido");
		alike.codigo_error();
	}


	public void esperaba_tipo(Symbol.Types tipo) {
		System.out.println("ERROR: Se esperaba un tipo " + tipo);
		alike.codigo_error();
	}


	public void simbolo_definido(String id) {
		System.out.println("ERROR: El simbolo " + id + " ya está definido");
		alike.codigo_error();
	}

	
	public void simbolo_no_es(String id, String tipo) {
		System.out.println("ERROR: El simbolo " + id + " no es un " + tipo);
		alike.codigo_error();
	}
}
