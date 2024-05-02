//*****************************************************************
// File:   SemanticFunction.java
// Author: Jorge Rodilla Esteve 845389 Enrique Martinez Casanova 839773
// Date:   marzo 2024
//*****************************************************************

package lib.attributes;

import lib.symbolTable.*;
import lib.symbolTable.exceptions.*;
import lib.tools.codeGeneration.PCodeInstruction;

import java.util.*;
import lib.attributes.*;
import traductor.*;

public class SemanticFunction {

    public void Declaracion(ArrayList<String> array_nombres_variables, Symbol tipo_variables, SymbolTable st, Token linea) {
        // Se tiene un simbolo sin nombre y un array de Strings
		for (String variable : array_nombres_variables) {
			tipo_variables.name = variable.toLowerCase();
			try {st.insertSymbol(tipo_variables.clone());}	// Se inserta una copia del puntero
			catch (AlreadyDefinedSymbolException e) {
				simbolo_definido(variable, linea.beginLine, linea.beginColumn);
			}
			catch (IndexArrayNotCorrect i) {	// Caso de simbolo array con indices incorrectos
				error("Los indices del array " + variable + " no son correctos", linea.beginLine, linea.beginColumn);
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


    public void Primario_ID(Token t, Trio tipo, SymbolTable st) {
    	try {
			Symbol simbolo = st.getSymbol(t.image.toLowerCase());

			if (simbolo.type == Symbol.Types.INT) {
				tipo.tipo = Symbol.Types.INT;
				tipo.referencia = true;
				tipo.nombre = t.image;
			}
			else if (simbolo.type == Symbol.Types.BOOL) {
				tipo.tipo = Symbol.Types.BOOL;
				tipo.referencia = true;
				tipo.nombre = t.image;
			}
			else if (simbolo.type == Symbol.Types.CHAR) {
				tipo.tipo = Symbol.Types.CHAR;
				tipo.referencia = true;
				tipo.nombre = t.image;
			}
			else if (simbolo.type == Symbol.Types.ARRAY) {
				tipo.tipo = Symbol.Types.ARRAY;
				tipo.referencia = true;
				tipo.nombre = t.image;
			}
			else if (simbolo.type == Symbol.Types.FUNCTION) {
				SymbolFunction s = (SymbolFunction) simbolo;

				// Se verifica que la función no tiene parámetros
				if (s.parList.isEmpty()) {
					// Se asigna el tipo que retorna la función
					tipo.tipo = s.returnType;
				}
				else {
					// Se ha utilizado una función que tiene parámetros como una que no los tiene
					error("Los parámetros de la llamada a " + t.image + " no son correctos", t.beginLine, t.beginColumn);
				}
			}
		}
		catch (SymbolNotFoundException s) {
			simbolo_no_definido(t.image, t.beginLine, t.beginColumn);
		}
    }


	public void Asignacion(String id, Trio tipo_asignacion, SymbolTable st, Token linea) {
		try {
			Symbol simbolo_asignacion = st.getSymbol(id);
			Symbol.Types tipo_id = simbolo_asignacion.type;

			if (tipo_id != tipo_asignacion.tipo || tipo_id == Symbol.Types.FUNCTION || tipo_id == Symbol.Types.PROCEDURE
				|| tipo_id == Symbol.Types.STRING || tipo_id == Symbol.Types.UNDEFINED || tipo_id == Symbol.Types.ARRAY) {
					esperaba_tipo(tipo_id, linea.beginLine, linea.beginColumn);
			}
		}
		catch (SymbolNotFoundException s) {
			simbolo_no_definido(id, linea.beginLine, linea.beginColumn);
		}
	}


	public void Instr_funcion_vector_3(String id, Trio tipo_primera_expresion, Trio tipo_asignacion, SymbolTable st, Token t) {
		try {
			Symbol simbolo = st.getSymbol(id);
			SymbolArray simbolo_array = (SymbolArray) simbolo;

			if (tipo_primera_expresion.tipo != Symbol.Types.INT) {
				esperaba_tipo(Symbol.Types.INT, t.beginLine, t.beginColumn);
			}

			// No se verifica que la expresión a la derecha pueda ser string, función, array o procedimiento
			// A nivel sintáctico, un array sólo puede ser de tipo INT, BOOL o CHAR
			if (tipo_asignacion.tipo != simbolo_array.baseType) {
				esperaba_tipo(simbolo_array.baseType, t.beginLine, t.endColumn);
			}
		}
		catch (SymbolNotFoundException s) {
			simbolo_no_definido(id, t.beginLine, t.beginColumn);
		}
		catch (ClassCastException e) {	// Excepción que saltará si se intenta meter un simbolo que no es un array en un SymbolArray
			simbolo_no_es(id, "array", t.beginLine, t.beginColumn);
		}
	}


	public void comprobar_funciones_especiales(String id_funcion) throws SpecialFunctionFound {
		String id = id_funcion;
		if (id.equals("put_line") || id.equals("put") || id.equals("get")) {
			throw new SpecialFunctionFound();
		}
	}


	public Trio llamada_funcion(SymbolFunction simbolo_funcion, ArrayList<Trio> lista_argumentos, SymbolTable st, Token t) {
		Trio resultado = new Trio();
		resultado.tipo = simbolo_funcion.returnType;
		ArrayList<Symbol> lista_parametros = simbolo_funcion.parList;
		verificar_argumentos(lista_parametros, lista_argumentos, st, t);
		return resultado;
	}


	public Trio indice_array(SymbolArray simbolo_array, Trio indice, Token t) {
		Trio resultado = new Trio();

		// La expresión para calcular el índice no es de tipo INT
		if (indice.tipo != Symbol.Types.INT) {
			esperaba_tipo(Symbol.Types.INT, t.beginLine, t.beginColumn);
		}
		else {
			resultado.tipo = simbolo_array.baseType;
			resultado.referencia = true;
		}
		return resultado;
	}


	public Trio verificar_expresion(Token t, ArrayList<Trio> lista_argumentos, SymbolTable st) {
		String id = t.image.toLowerCase();
		Trio resultado = new Trio();
		try {
			comprobar_funciones_especiales(id);

			Symbol simbolo = st.getSymbol(id);

			if (simbolo.type == Symbol.Types.FUNCTION) {
				SymbolFunction simbolo_funcion = (SymbolFunction) simbolo;
				resultado = llamada_funcion(simbolo_funcion, lista_argumentos, st, t);
			}
			else if (simbolo.type == Symbol.Types.ARRAY) {
				SymbolArray simbolo_array = (SymbolArray) simbolo;
				if (lista_argumentos.size() == 1) {
					resultado = indice_array(simbolo_array, lista_argumentos.get(0), t);
				}
				else {
					error("Se esperaba un único índice en el array " + id, t.beginLine, t.beginColumn);
				}
			}
		}
		catch (SymbolNotFoundException s) {
			simbolo_no_definido(id, t.beginLine, t.beginColumn);
		}
		// Se captura la excepción pero no saca error, para evitar el error de símbolo no definido,
		// el tipo que se va a devolver es UNDEFINED, que dará error en la llamada a la función que anida a esta otra llamada
		catch (SpecialFunctionFound g) {}
		return resultado;
	}


	public void verificar_argumentos(ArrayList<Symbol> lista_parametros, ArrayList<Trio> lista_argumentos, SymbolTable st, Token t) {
		int numero_parametros = lista_parametros.size();
		if (numero_parametros == lista_argumentos.size()) {
			for (int i = 0; i < numero_parametros; i++) {

				Symbol parametro = lista_parametros.get(i);
				Trio argumento = lista_argumentos.get(i);

				// Tipos no coinciden
				if (argumento.tipo != parametro.type) {
					esperaba_tipo(parametro.type, t.beginLine, t.beginColumn);
				}

				// Tipo por referencia y no es asignable
				if (parametro.parClass == Symbol.ParameterClass.REF && !argumento.referencia) {
					tipo_asignable(t.beginLine, t.beginColumn);
				}

				// Caso ARRAY
				if (parametro.type == Symbol.Types.ARRAY) {
					if (argumento.tipo == Symbol.Types.ARRAY) {
						SymbolArray array_argumento = (SymbolArray) (st.getSymbol(argumento.nombre));
						SymbolArray array_parametro = (SymbolArray) (parametro);

						// Índices no coinciden
						if (array_argumento.minInd != array_parametro.minInd || array_argumento.maxInd != array_parametro.maxInd) {
							error("Los índices del array parámetro " + parametro.name + " no coinciden", t.beginLine, t.beginColumn);
						}

						// Tipos base no coinciden
						if (array_argumento.baseType != array_parametro.baseType) {
							error("Los tipos base del array parámetro " + parametro.name + " no coinciden", t.beginLine, t.beginColumn);
						}
					}
					else {
						// Se esperaba un array
						esperaba_tipo(parametro.type, t.beginLine, t.beginColumn);
					}
				}
			}
		}
		else {
			error("Se esperaban " + numero_parametros + " parametros", t.beginLine, t.beginColumn);
		}
	}


	public void llamada_procedimiento(String id, ArrayList<Trio> lista_argumentos, SymbolTable st, Token t) {
		try {
			comprobar_funciones_especiales(id);

			Symbol simbolo = st.getSymbol(id);

			SymbolProcedure simbolo_procedimiento = (SymbolProcedure) simbolo;
			ArrayList<Symbol> lista_parametros = simbolo_procedimiento.parList;

			verificar_argumentos(lista_parametros, lista_argumentos, st, t);
		}
		catch (SymbolNotFoundException s) {
			simbolo_no_definido(id, t.beginLine, t.beginColumn);
		}
		catch (ClassCastException e) {
			simbolo_no_es(id, "procedimiento", t.beginLine, t.beginColumn);
		}
		catch (SpecialFunctionFound g) {
			int numero_argumentos = lista_argumentos.size();
			if (numero_argumentos > 0 || id.equals("put_line")) {
				for (int i = 0; i < numero_argumentos; i++) {
					Trio argumento = lista_argumentos.get(i);
					if (id.equals("get") && (argumento.tipo != Symbol.Types.INT && argumento.tipo != Symbol.Types.CHAR)) {
						error("Se esperaba un tipo INT/CHAR", t.beginLine, t.beginColumn);
					}
					else if (id.equals("get") && !argumento.referencia) {
						tipo_asignable(t.beginLine, t.beginColumn);
					}
					else if (id.equals("get")) {
						try {
							Symbol simbolo = st.getSymbol(lista_argumentos.get(i).nombre);
							int tipo;
							if (simbolo.type == Symbol.Types.INT) {
								tipo = 1;
							}
							else if (simbolo.type == Symbol.Types.CHAR) {
								tipo = 0;
							}
							bloque.addComment("Leer");
							bloque.addComment("Direccion de variable" + lista_argumentos.get(i).nombre);
							bloque.addInst(PCodeInstruction.OpCode.SRF, alike.nivel_bloque - simbolo.nivel, simbolo.dir);
							bloque.addInst(PCodeInstruction.OpCode.RD, tipo);

						} catch (SymbolNotFoundException e){
							simbolo_no_definido(id, t.beginLine, t.beginColumn);
						}

					}
					if ((id.equals("put") || id.equals("put_line")) && (argumento.tipo != Symbol.Types.INT &&
						argumento.tipo != Symbol.Types.BOOL && argumento.tipo != Symbol.Types.CHAR && argumento.tipo != Symbol.Types.STRING)) {
							error("Se esperaba un tipo INT/BOOL/CHAR/STRING", t.beginLine, t.beginColumn);
					}
				}
			}
			else {
				error("Los parámetros de la función " + id + " no son vacíos", t.beginLine, t.beginColumn);
			}
		}
	}


	public void verificar_bool(Symbol.Types tipo, Token t) {
		if (tipo != Symbol.Types.BOOL) {
			esperaba_tipo(Symbol.Types.BOOL, t.beginLine, t.endColumn);
		}
	}


	public void verificar_relacion(Token t, Trio tipo, Trio tipo2) {
		String operador = t.image;
		tipo.referencia = false;
		if (operador.equals("<") || operador.equals(">") || operador.equals("<=") || operador.equals(">=")) {
			if (tipo.tipo != Symbol.Types.INT || tipo2.tipo != Symbol.Types.INT) {
				tipo.tipo = Symbol.Types.UNDEFINED;
				esperaba_tipo(Symbol.Types.INT, t.beginLine, t.endColumn);
			}
			else tipo.tipo = Symbol.Types.BOOL;
		}
		else if (tipo2.tipo != Symbol.Types.ARRAY || tipo2.tipo != Symbol.Types.FUNCTION ||
				tipo2.tipo != Symbol.Types.PROCEDURE || tipo2.tipo != Symbol.Types.STRING) {
			if (tipo.tipo != tipo2.tipo) {
				tipo.tipo = Symbol.Types.UNDEFINED;
				esperaba_tipo(tipo.tipo, t.beginLine, t.endColumn);
			}
			else tipo.tipo = Symbol.Types.BOOL;
		}
		else {
			error("Se esperaba un tipo INT/BOOL/CHAR", t.beginLine, t.endColumn);
		}
	}


	public void verificar_int(Trio tipo, Trio tipo2, Token t) {
		// Si se ha hecho matching con ()*, la expresión ya NO es sólo una variable
		tipo.referencia = false;
		if (tipo.tipo != Symbol.Types.INT || tipo2.tipo != Symbol.Types.INT) {
			tipo.tipo = Symbol.Types.UNDEFINED;
			esperaba_tipo(Symbol.Types.INT, t.beginLine, t.endColumn);
		}
	}


	public void error(String error, int linea, int columna) {
		String mensaje = "ERROR (" + Integer.toString(linea) + ", " + Integer.toString(columna) + "): " + error;
		System.out.println(mensaje);
		alike.codigo_error();
	}


	public void simbolo_no_definido(String id, int linea, int columna) {
		error("El simbolo " + id + " no está definido", linea, columna);
	}


	public void esperaba_tipo(Symbol.Types tipo, int linea, int columna) {
		error("Se esperaba un tipo " + tipo, linea, columna);
	}


	public void simbolo_definido(String id, int linea, int columna) {
		error("El simbolo " + id + " ya está definido", linea, columna);
	}


	public void simbolo_no_es(String id, String tipo, int linea, int columna) {
		error("El simbolo " + id + " no es un " + tipo, linea, columna);
	}

	public void tipo_asignable(int linea, int columna) {
		error("Se esperaba un tipo asignable", linea, columna);
	}

}
