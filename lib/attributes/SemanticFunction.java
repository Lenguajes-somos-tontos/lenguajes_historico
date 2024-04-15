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
			}
			else if (simbolo.type == Symbol.Types.BOOL) {
				tipo.primero = Symbol.Types.BOOL;
				tipo.segundo = true;
			}
			else if (simbolo.type == Symbol.Types.CHAR) {
				tipo.primero = Symbol.Types.CHAR;
				tipo.segundo = true;
			}
			else if (simbolo.type == Symbol.Types.ARRAY) {
				tipo.primero = Symbol.Types.ARRAY;
				tipo.segundo = true;
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

/*
    public Par Factor(Par tipo, Par tipo2, boolean match2) {     
		if (match2 && tipo2.primero != Symbol.Types.BOOL) {
			tipo.primero = Symbol.Types.UNDEFINED;
		}
		else if (match2) {
			tipo.primero = tipo2.primero;	// tipo.primero = BOOL
			tipo.segundo = false;
		}
		return tipo;
	}


    public Par Termino(Par tipo, Par tipo2, boolean match2) {     
		if (match2 && (tipo.primero != Symbol.Types.INT || tipo2.primero != Symbol.Types.INT)) { 
			tipo.primero = Symbol.Types.UNDEFINED;
		}
		else if (match2) {
			tipo.segundo = false;
		}
		return tipo;
	}

    public Par Expresion_simple(Par tipo, Par tipo2, boolean match1, boolean match2) {     
		if ((match1 && tipo.primero != Symbol.Types.INT) || (match2 && (tipo.primero != Symbol.Types.INT || tipo2.primero != Symbol.Types.INT))) {
			tipo.primero = Symbol.Types.UNDEFINED;
		}
		else if (match1 || match2) {
			tipo.segundo = false;
		}
		return tipo;
	}

    public Par Relacion(Par tipo, Par tipo2, Token op, boolean match2) {     
		if (match2) {
			if (tipo.primero != tipo2.primero) {
				tipo.primero = Symbol.Types.UNDEFINED; 
			}
			tipo.segundo = false;
		}
		if (tipo.primero != Symbol.Types.UNDEFINED) {
			if (match2 && (op.image.equals("=") || op.image.equals("/="))) {
				if (tipo.primero == Symbol.Types.STRING || tipo.primero == Symbol.Types.ARRAY) {
					tipo.primero = Symbol.Types.UNDEFINED;
				}
				else {
					tipo.primero = Symbol.Types.BOOL;
				}
			}
			else if (match2 && (!op.image.equals("=") || !op.image.equals("/="))) {
				if (tipo.primero != Symbol.Types.INT || tipo.primero == Symbol.Types.CHAR) {
					tipo.primero = Symbol.Types.UNDEFINED;
				}
				else {
					tipo.primero = Symbol.Types.BOOL;
				}
			}
		}
		return tipo;
	}
/*
    public Par Expresion(Par tipo, Par tipo2, boolean match2) {     
		if (match2 && (tipo.primero != Symbol.Types.BOOL || tipo2.primero != Symbol.Types.BOOL)) {
			tipo.primero = Symbol.Types.UNDEFINED;
		}
		else if (match2) {
			tipo.segundo = false;
		}
		System.out.println("Expresion: " + tipo.primero + " " + tipo.segundo);
		return tipo;
	}
*/
	public void Asignacion(Token id, Par tipo_asignacion, Symbol simbolo_asignacion, SymbolTable st) {
		try {
			simbolo_asignacion = st.getSymbol(id.image);

			Symbol.Types tipo_id = simbolo_asignacion.type;
			if (!((tipo_id == tipo_asignacion.primero) &&
				((tipo_id != Symbol.Types.FUNCTION) || (tipo_id != Symbol.Types.PROCEDURE) || tipo_id != Symbol.Types.ARRAY))) {
				System.out.println("ERROR: Se esperaba un tipo " + tipo_id);
			}
		}
		catch (SymbolNotFoundException s) {
			System.out.println("ERROR: El simbolo " + id.image + " no está definido");
		}
	}

	public void Instruccion(Token id, boolean llamada_funcion_simple, SymbolTable st) {
		// Si se entra al if, es que se ha llamado a un procedimiento/función
		if (llamada_funcion_simple) {
			try {
				Symbol simbolo_proc_func = st.getSymbol(id.image);

				if ((simbolo_proc_func.type != Symbol.Types.FUNCTION) && (simbolo_proc_func.type != Symbol.Types.PROCEDURE)) {
					System.out.println("ERROR: Se esperaba un procedimiento o función");
				}
				else {
					// El ID que se ha escrito es un procedimiento/función, verificar que no tiene parámetros
					if (simbolo_proc_func.type == Symbol.Types.FUNCTION) {
						SymbolFunction s = (SymbolFunction) simbolo_proc_func;
						if (!s.parList.isEmpty()) {
							System.out.println("ERROR: Los parámetros de la función " + id.image + " no son correctos");
						}
					}
					else {
						SymbolProcedure s = (SymbolProcedure) simbolo_proc_func;
						if (!s.parList.isEmpty()) {
							System.out.println("ERROR: Los parámetros del procedimiento " + id.image + " no son correctos");
						}
					}
				}
			}
			catch (SymbolNotFoundException s) {
				System.out.println("ERROR: El procedimiento o función " + id.image + " no está definido");
			}
		}
	}

	public void Instr_funcion_vector_3(String id, Par tipo_primera_expresion, Par tipo_asignacion, SymbolTable st) {
		try {
			Symbol simbolo = st.getSymbol(id);
			SymbolArray simbolo_array = (SymbolArray) simbolo;

			if (tipo_primera_expresion.primero != Symbol.Types.INT) {
				System.out.println("ERROR: Se esperaba una expresión de tipo INT");
			}
			else {
				if (tipo_asignacion.primero != simbolo_array.baseType) {
					// No se verifica que la expresión a la derecha pueda ser string, función, array o procedimiento
					// A nivel sintáctico, un array sólo puede ser de tipo INT, BOOL o CHAR
					System.out.println("ERROR: Se esperaba una expresión de tipo " + simbolo_array.baseType);
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

	public void verificar_parametro_procedimiento(SymbolProcedure p, Par par, int iterador) {
		if (p.parList.get(iterador).type != par.primero) {
			System.out.println("ERROR: Se esperaba un tipo " + p.parList.get(iterador).type + " en la llamada a " + p.name); // Y la línea
		}
		else if (p.parList.get(iterador).parClass == Symbol.ParameterClass.REF && !par.segundo) {
			System.out.println("ERROR: El tipo " + p.parList.get(iterador).type + " en la llamada a " + p.name + " debe ser una variable"); // Y la línea
		}
	}

	public void verificar_parametro_funcion(SymbolFunction p, Par par, int iterador) {
		if (p.parList.get(iterador).type != par.primero) {
			System.out.println("ERROR: Se esperaba un tipo " + p.parList.get(iterador).type + " en la llamada a " + p.name); // Y la línea
		}
		else if (p.parList.get(iterador).parClass == Symbol.ParameterClass.REF && !par.segundo) {
			System.out.println("ERROR: El tipo " + p.parList.get(iterador).type + " en la llamada a " + p.name + " debe ser una variable"); // Y la línea
		}
	}
}
