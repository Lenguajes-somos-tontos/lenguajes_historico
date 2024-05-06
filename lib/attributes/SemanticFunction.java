//*****************************************************************
// File:   SemanticFunction.java
// Author: Jorge Rodilla Esteve 845389 Enrique Martinez Casanova 839773
// Date:   marzo 2024
//*****************************************************************

package lib.attributes;

import lib.symbolTable.*;
import lib.symbolTable.exceptions.*;
import lib.tools.codeGeneration.CGUtils;
import lib.tools.codeGeneration.PCodeInstruction;

import java.util.*;
import lib.attributes.*;
import traductor.*;

public class SemanticFunction {

    public void Declaracion(ArrayList<String> array_nombres_variables, Symbol tipo_variables, SymbolTable st, Token linea) {
        // Se tiene un simbolo sin nombre y un array de Strings
		for (String variable : array_nombres_variables) {
			tipo_variables.name = variable.toLowerCase();
			try {	// Se inserta una copia del puntero
				Symbol clon = tipo_variables.clone();
				st.insertSymbol(clon);
				clon.nivel = clon.nivel - 1;
				clon.dir = alike.sig[alike.nivel_bloque];
				if (clon.type == Symbol.Types.ARRAY) {
					SymbolArray s = (SymbolArray) clon;
					alike.sig[alike.nivel_bloque] += (s.maxInd - s.minInd + 1);
				}
				else
					alike.sig[alike.nivel_bloque]++;
			}
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


    public Symbol Tipo_array(Token indMin, Token indMax, Symbol tipo_base, Token ind_menor, Token ind_mayor) {
		int indMin_i, indMax_i;

		if (ind_menor == null || ind_menor.image == "+") {
			indMin_i = Integer.parseInt(indMin.image);
		}
		else {
			indMin_i = -Integer.parseInt(indMin.image);
		}

		if (ind_mayor == null || ind_mayor.image == "+") {
			indMax_i = Integer.parseInt(indMax.image);
		}
		else {
			indMax_i = -Integer.parseInt(indMax.image);
		}

		Symbol result = new SymbolArray("", indMin_i, indMax_i, tipo_base.type);
		return result;
    }


    public void Primario_ID(Token t, Trio tipo, SymbolTable st) {
    	try {
			Symbol simbolo = st.getSymbol(t.image.toLowerCase());

			alike.bloque.addComment(" Leer la direccion de la variable " + simbolo.name);
			alike.bloque.addInst(PCodeInstruction.OpCode.SRF, alike.nivel_bloque - simbolo.nivel, (int)simbolo.dir);
			// Revisar caso de que expresión sea SOLO un array
				alike.bloque.addInst(PCodeInstruction.OpCode.DRF);
			if (simbolo.type == Symbol.Types.INT) {
				tipo.tipo = Symbol.Types.INT;
				tipo.referencia = true;
				tipo.simbolo = simbolo;
			}
			else if (simbolo.type == Symbol.Types.BOOL) {
				tipo.tipo = Symbol.Types.BOOL;
				tipo.referencia = true;
				tipo.simbolo = simbolo;
			}
			else if (simbolo.type == Symbol.Types.CHAR) {
				tipo.tipo = Symbol.Types.CHAR;
				tipo.referencia = true;
				tipo.simbolo = simbolo;
			}
			else if (simbolo.type == Symbol.Types.ARRAY) {
				tipo.tipo = Symbol.Types.ARRAY;
				tipo.referencia = true;
				tipo.simbolo = simbolo;
			}
			else if (simbolo.type == Symbol.Types.FUNCTION) {
				SymbolFunction s = (SymbolFunction) simbolo;

				// Se verifica que la función no tiene parámetros
				if (s.parList.isEmpty()) {
					// Se asigna el tipo que retorna la función
					tipo.tipo = s.returnType;
					tipo.simbolo.type = s.returnType;
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
		if (id.equals("put_line") || id.equals("put") || id.equals("get") || id.equals("skip_line") || id.equals("int2char") || id.equals("char2int")) {
			throw new SpecialFunctionFound();
		}
	}

	/*
	public Trio llamada_funcion(SymbolFunction simbolo_funcion, ArrayList<Trio> lista_argumentos, SymbolTable st, Token t) {
		Trio resultado = new Trio();
		resultado.tipo = simbolo_funcion.returnType;
		ArrayList<Symbol> lista_parametros = simbolo_funcion.parList;
		verificar_argumentos(lista_parametros, lista_argumentos, st, t);
		return resultado;
	}
	*/

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

	/*
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
	*/

	public ArrayList<Symbol> proc_param(String id, Token t, SymbolTable st) {
		ArrayList<Symbol> result = null;
		try {
			comprobar_funciones_especiales(id);
			Symbol func_proc = st.getSymbol(id);
			if (func_proc.type == Symbol.Types.PROCEDURE) {
				SymbolProcedure s = (SymbolProcedure) func_proc;
				result = s.parList;
			}
			else {
				esperaba_tipo(Symbol.Types.PROCEDURE, t.beginLine, t.beginColumn);
			}
		}
		catch (SymbolNotFoundException e) {
			simbolo_no_definido(id, t.beginLine, t.beginColumn);
		}
		catch (SpecialFunctionFound s) {
			if (id.equals("skip_line")) {
				error("Se esperaban 0 argumentos", t.beginLine, t.beginColumn);
			}
			// Llamar a int2char o char2int como procedimientos es error sintáctico
		}
		return result;
	}


	public void procedimientos_especiales(String id, Trio tipo, Token t) {
		if (id.equals("put") || id.equals("put_line")) {
			if (tipo.tipo == Symbol.Types.INT)
				alike.bloque.addInst(PCodeInstruction.OpCode.WRT, 1);
			else if (tipo.tipo == Symbol.Types.CHAR)
				alike.bloque.addInst(PCodeInstruction.OpCode.WRT, 0);
			else if (tipo.tipo == Symbol.Types.BOOL) {
				String label1 = CGUtils.newLabel();
				String label2 = CGUtils.newLabel();
				alike.bloque.addInst(PCodeInstruction.OpCode.JMF, label2);
				alike.bloque.addInst(PCodeInstruction.OpCode.STC, 116);
				alike.bloque.addInst(PCodeInstruction.OpCode.WRT, 0);
				alike.bloque.addInst(PCodeInstruction.OpCode.STC, 114);
				alike.bloque.addInst(PCodeInstruction.OpCode.WRT, 0);
				alike.bloque.addInst(PCodeInstruction.OpCode.STC, 117);
				alike.bloque.addInst(PCodeInstruction.OpCode.WRT, 0);
				alike.bloque.addInst(PCodeInstruction.OpCode.STC, 101);
				alike.bloque.addInst(PCodeInstruction.OpCode.WRT, 0);
				alike.bloque.addInst(PCodeInstruction.OpCode.JMP, label1);
				alike.bloque.addLabel(label2);
				alike.bloque.addInst(PCodeInstruction.OpCode.STC, 102);
				alike.bloque.addInst(PCodeInstruction.OpCode.WRT, 0);
				alike.bloque.addInst(PCodeInstruction.OpCode.STC, 97);
				alike.bloque.addInst(PCodeInstruction.OpCode.WRT, 0);
				alike.bloque.addInst(PCodeInstruction.OpCode.STC, 108);
				alike.bloque.addInst(PCodeInstruction.OpCode.WRT, 0);
				alike.bloque.addInst(PCodeInstruction.OpCode.STC, 115);
				alike.bloque.addInst(PCodeInstruction.OpCode.WRT, 0);
				alike.bloque.addInst(PCodeInstruction.OpCode.STC, 101);
				alike.bloque.addInst(PCodeInstruction.OpCode.WRT, 0);
				alike.bloque.addLabel(label1);
			}
			else if (tipo.tipo == Symbol.Types.STRING) {
				for (char c : tipo.nombre.toCharArray()) {
					if (c != '\"') {
						alike.bloque.addInst(PCodeInstruction.OpCode.STC, c);
						alike.bloque.addInst(PCodeInstruction.OpCode.WRT, 0);
					}
				}
			}
			else {
				error("Se esperaba un tipo INT/BOOL/CHAR", t.beginLine, t.beginColumn);
			}
		}
		else if (id.equals("get")) {
			if (!tipo.referencia)
				error("Se esperaba un tipo asignable", t.beginLine, t.endColumn);
			else if (tipo.tipo == Symbol.Types.INT) {
				alike.bloque.removeLastInst();
				alike.bloque.addInst(PCodeInstruction.OpCode.RD, 1);
			}
			else if (tipo.tipo == Symbol.Types.CHAR) {
				alike.bloque.removeLastInst();
				alike.bloque.addInst(PCodeInstruction.OpCode.RD, 0);
			}
			else
				error("Se esperaba un tipo INT/CHAR", t.beginLine, t.endColumn);
		}
	}


	// Pre: El parámetro con índice "indice" existe en "lista_parametros"
	public void verificar_argumento(Trio argumento, ArrayList<Symbol> lista_parametros, int indice, SymbolTable st, Token t) {
		Symbol parametro = lista_parametros.get(indice);

		// Tipos no coinciden
		if (argumento.tipo != parametro.type) {
			esperaba_tipo(parametro.type, t.beginLine, t.beginColumn);
		}

		// Tipo por referencia y no es asignable
		if (parametro.parClass == Symbol.ParameterClass.REF && !argumento.referencia) {
			tipo_asignable(t.beginLine, t.beginColumn);
		}
		else if (parametro.parClass == Symbol.ParameterClass.REF) {
			alike.bloque.removeLastInst();
		}

		// Caso ARRAY
		if (parametro.type == Symbol.Types.ARRAY) {
			if (argumento.tipo == Symbol.Types.ARRAY) {
				SymbolArray array_argumento = (SymbolArray) (argumento.simbolo);
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


	public void verificar_bool(Symbol.Types tipo, Token t) {
		if (tipo != Symbol.Types.BOOL) {
			esperaba_tipo(Symbol.Types.BOOL, t.beginLine, t.endColumn);
		}
	}


	public void verificar_bool_expresion(Trio tipo, Trio tipo2, Token t) {
		tipo.referencia = false;
		if (tipo.tipo != Symbol.Types.BOOL || tipo2.tipo != Symbol.Types.BOOL) {
			esperaba_tipo(Symbol.Types.BOOL, t.beginLine, t.endColumn);
			tipo.tipo = Symbol.Types.UNDEFINED;
		}
		else {
			switch (t.image.toLowerCase()) {
				case "or":
					alike.bloque.addInst(PCodeInstruction.OpCode.OR);
					break;
				case "and":
					alike.bloque.addInst(PCodeInstruction.OpCode.AND);
					break;	
			}
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
			else {
				tipo.tipo = Symbol.Types.BOOL;
				switch (operador) {
					case "<":
						alike.bloque.addInst(PCodeInstruction.OpCode.LT);
						break;
					case ">":
						alike.bloque.addInst(PCodeInstruction.OpCode.GT);
						break;
					case "<=":
						alike.bloque.addInst(PCodeInstruction.OpCode.LTE);
						break;
					case ">=":
						alike.bloque.addInst(PCodeInstruction.OpCode.GTE);
						break;	
				}
			}
		}
		else if (tipo2.tipo != Symbol.Types.ARRAY || tipo2.tipo != Symbol.Types.FUNCTION ||
				tipo2.tipo != Symbol.Types.PROCEDURE || tipo2.tipo != Symbol.Types.STRING) {
			if (tipo.tipo != tipo2.tipo) {
				tipo.tipo = Symbol.Types.UNDEFINED;
				esperaba_tipo(tipo.tipo, t.beginLine, t.endColumn);
			}
			else {
				tipo.tipo = Symbol.Types.BOOL;
				switch (operador) {
					case "=":
						alike.bloque.addInst(PCodeInstruction.OpCode.EQ);
						break;
					case "/=":
						alike.bloque.addInst(PCodeInstruction.OpCode.NEQ);
						break;
				}
			}
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
		else {
			switch (t.image.toLowerCase()) {
				case "*":
					alike.bloque.addInst(PCodeInstruction.OpCode.TMS);
					break;
				case "/":
					alike.bloque.addInst(PCodeInstruction.OpCode.DIV);
					break;
				case "mod":
					alike.bloque.addInst(PCodeInstruction.OpCode.MOD);
					break;
				case "+":
					alike.bloque.addInst(PCodeInstruction.OpCode.PLUS);
					break;
				case "-":
					alike.bloque.addInst(PCodeInstruction.OpCode.SBT);
					break;
			}
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
