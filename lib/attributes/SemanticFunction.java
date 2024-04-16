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



    public void Primario_ID(Token t, Par tipo, Symbol simbolo, SymbolTable st) {
    	try {
			simbolo = st.getSymbol(t.image);

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
					System.out.println("ERROR: La función " + t.image + " tiene parámetros");
					// Se ha utilizado una función que tiene parámetros como una que no los tiene
				}
			}
			else {
				System.out.println("ERROR: El tipo de " + t.image + " no es correcto");
				// El ID es un procedimiento, error
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



	public void Instruccion(String id, SymbolTable st) {
		try {
			Symbol simbolo_proc_func = st.getSymbol(id);

			if (simbolo_proc_func.type != Symbol.Types.FUNCTION && simbolo_proc_func.type != Symbol.Types.PROCEDURE) {
				System.out.println("ERROR: Se esperaba un procedimiento o función");
			}
			else {
				// El ID que se ha escrito es un procedimiento/función, verificar que no tiene parámetros
				ArrayList<Symbol> lista_parametros;
				if (simbolo_proc_func.type == Symbol.Types.FUNCTION) {
					SymbolFunction s = (SymbolFunction) simbolo_proc_func;
					lista_parametros = s.parList;
				}
				else {
					SymbolProcedure s = (SymbolProcedure) simbolo_proc_func;
					lista_parametros = s.parList;
				}
				if (!lista_parametros.isEmpty()) {
					System.out.println("ERROR: Los parámetros de la llamada a " + id + " no son correctos");
				}
			}
		}
		catch (SymbolNotFoundException s) {
			System.out.println("ERROR: El procedimiento o función " + id + " no está definido");
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
}
