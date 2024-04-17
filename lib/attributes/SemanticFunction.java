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
				System.out.println("Simbolo " + variable + " ya definido");
			}
			catch (IndexArrayNotCorrect i) {	// Caso de simbolo array con indices incorrectos
				System.out.println("Los indices del array " + variable + " no son correctos");
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
					// Se ha utilizado una función que tiene parámetros como una que no los tiene
				}
			}
		}
		catch (SymbolNotFoundException s) {
			System.out.println("ERROR: El símbolo " + t.image + " no está definido");
		}
    }



	public void Asignacion(String id, Par tipo_asignacion, SymbolTable st) {
		try {
			Symbol simbolo_asignacion = st.getSymbol(id);
			Symbol.Types tipo_id = simbolo_asignacion.type;

			if (tipo_id != tipo_asignacion.primero || tipo_id == Symbol.Types.FUNCTION
				|| tipo_id == Symbol.Types.PROCEDURE || tipo_id == Symbol.Types.ARRAY || tipo_id == Symbol.Types.STRING) {
				System.out.println("ERROR: Se esperaba un tipo " + tipo_id);
			}
		}
		catch (SymbolNotFoundException s) {
			System.out.println("ERROR: El simbolo " + id + " no está definido");
		}
	}



	public void Instr_funcion_vector_3(String id, Par tipo_primera_expresion, Par tipo_asignacion, SymbolTable st) {
		try {
			Symbol simbolo = st.getSymbol(id);
			SymbolArray simbolo_array = (SymbolArray) simbolo;

			if (tipo_primera_expresion.primero != Symbol.Types.INT) {
				System.out.println("ERROR: Se esperaba una expresión de tipo INT para el índice del array " + id);
			}
			else {
				if (tipo_asignacion.primero != simbolo_array.baseType) {
					// No se verifica que la expresión a la derecha pueda ser string, función, array o procedimiento
					// A nivel sintáctico, un array sólo puede ser de tipo INT, BOOL o CHAR
					System.out.println("ERROR: Se esperaba una expresión de tipo " + simbolo_array.baseType + "en la asignación al array " + id);
				}
			}
		}
		catch (SymbolNotFoundException s) {
			System.out.println("ERROR: El símbolo " + id + " no está definido");
		}
		catch (ClassCastException e) {	// Excepción que saltará si se intenta meter un simbolo que no es un array en un SymbolArray
			System.out.println("ERROR: El símbolo " + id + " no es un array");
		}
	}



	public void comprobar_funciones_especiales(String id_funcion) throws SpecialFunctionFound {
		String id = id_funcion.toLowerCase();
		if (id.equals("put_line") || id.equals("put") || id.equals("get")) {
			throw new SpecialFunctionFound();
		}
	}


	public Par llamada_funcion(Symbol simbolo_llamada, ArrayList<Par> lista_argumentos, SymbolTable st) {
		Par resultado = new Par();
		String id = "";
		id = simbolo_llamada.name;

		ArrayList<Symbol> lista_parametros;
		if (simbolo_llamada.type == Symbol.Types.FUNCTION) {
			SymbolFunction s = (SymbolFunction) simbolo_llamada;
			lista_parametros = s.parList;
			resultado.primero = s.returnType;
		}
		else {
			SymbolProcedure s = (SymbolProcedure) simbolo_llamada;
			lista_parametros = s.parList;
		}

		int numero_parametros = lista_parametros.size();
		if (numero_parametros == lista_argumentos.size()) {
			for (int i = 0; i < lista_parametros.size(); i++) {
				Symbol simbolo_parametro = lista_parametros.get(i);
				Par argumento = lista_argumentos.get(i);
				
				if (argumento.primero != simbolo_parametro.type) {
					System.out.println("ERROR: Se esperaba un tipo " + simbolo_parametro.type + " en la llamada a " + id);
				}
				if (simbolo_parametro.parClass == Symbol.ParameterClass.REF && !argumento.segundo) {
					System.out.println("ERROR: El parámetro " + simbolo_parametro.name + " es un parámetro por referencia");
				}
				// Caso ARRAY
				if (simbolo_parametro.type == Symbol.Types.ARRAY) {
					SymbolArray array_argumento = (SymbolArray) (st.getSymbol(argumento.tercero));
					SymbolArray array_parametro = (SymbolArray) (simbolo_parametro);

					if (array_argumento.minInd != array_parametro.minInd || array_argumento.maxInd != array_parametro.maxInd) {
						System.out.println("ERROR: Los índices del array parámetro " + simbolo_parametro.name + " no coinciden");
					}
					if (array_argumento.baseType != array_parametro.baseType) {
						System.out.println("ERROR: Los tipos base del array parámetro " + simbolo_parametro.name + " no coinciden");
					}
				}
			}
		}
		else {
			System.out.println("ERROR: El número de parámetros en la llamada a " + id + " no coinciden, se esperaban " + numero_parametros + " parametros");
		}
		return resultado;
	}


	public Par indice_array(SymbolArray simbolo_array, Par indice) {
		Par resultado = new Par();

		if (indice.primero != Symbol.Types.INT) {
			System.out.println("ERROR: Se esperaba una expresión de tipo INT");
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

			if (simbolo.type == Symbol.Types.FUNCTION || simbolo.type == Symbol.Types.PROCEDURE) {
				resultado = llamada_funcion(simbolo, lista_argumentos, st);
			}
			else if (simbolo.type == Symbol.Types.ARRAY) {
				SymbolArray simbolo_array = (SymbolArray) simbolo;
				if (lista_argumentos.size() == 1)
					resultado = indice_array(simbolo_array, lista_argumentos.get(0));
				else
					System.out.println("ERROR: Se esperaba un único índice en el array " + id);
			}
			else
				System.out.println("ERROR: Se esperaba un tipo FUNCTION/PROCEDURE");
		}
		catch (SymbolNotFoundException s) {
			System.out.println("ERROR: El simbolo " + id + " no está definido");
		}
		catch (SpecialFunctionFound g) {}
		return resultado;
	}



	public Par verificar_llamada(String id, ArrayList<Par> lista_argumentos, SymbolTable st) {
		Par resultado = new Par();
		try {
			comprobar_funciones_especiales(id);

			Symbol simbolo = st.getSymbol(id);

			if (simbolo.type == Symbol.Types.FUNCTION || simbolo.type == Symbol.Types.PROCEDURE)
				resultado = llamada_funcion(simbolo, lista_argumentos, st);
			else
				System.out.println("ERROR: Se esperaba un tipo FUNCTION/PROCEDURE");
		}
		catch (SymbolNotFoundException s) {
			System.out.println("ERROR: El simbolo " + id + " no está definido");
		}
		catch (SpecialFunctionFound g) {
			for (int i = 0; i < lista_argumentos.size(); i++) {
				Par argumento = lista_argumentos.get(i);
				if (id.equals("get") && (argumento.primero != Symbol.Types.INT && argumento.primero != Symbol.Types.CHAR)) {
					System.out.println("ERROR: Se esperaba un tipo INT/CHAR en la llamada a get");
				}
				else if ((id.equals("put") || id.equals("put_line")) && (argumento.primero != Symbol.Types.INT &&
						argumento.primero != Symbol.Types.BOOL && argumento.primero != Symbol.Types.CHAR && argumento.primero != Symbol.Types.STRING)) {
					System.out.println("ERROR: Se esperaba un tipo INT/BOOL/CHAR/STRING en la llamada a " + id);
				}
			}
		}
		return resultado;
	}



	public void verificar_bool(Symbol.Types tipo) {
		if (tipo != Symbol.Types.BOOL) {
			System.out.println("ERROR: Se esperaba una expresión booleana");
		}
	}
}
