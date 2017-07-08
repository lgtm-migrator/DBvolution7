/*
 * Copyright 2015 gregorygraham.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nz.co.gregs.dbvolution.internal.h2;

import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author gregorygraham
 */
public enum Point3DFunctions implements DBVFeature {

	/**
	 *
	 */
	CREATE("DBV_CREATE_POINT3D_FROM_COORDS", "String", "Double x, Double y, Double z", 
			"if (x == null || y == null || z == null) {\n" 
					+ "				return null;\n" 
					+ "			} else {\n" 
					+ "				return \"POINT (\" + x + \" \" + y + \" \" + z + \")\" ;\n" 
					+ "	}"),
	/**
	 *
	 */
	EQUALS("DBV_POINT3D_EQUALS", "Boolean", "String firstPoint, String secondPoint", "			if (firstPoint == null || secondPoint == null) {\n" + "				return null;\n" + "			} else {\n" + "				return firstPoint.equals(secondPoint);\n" + "			}"),
	/**
	 *
	 */
	GETX("DBV_POINT3D_GETX", "Double", "String firstPoint", "			if (firstPoint == null) {\n" + "				return null;\n" + "			} else {\n" + "				String[] split = firstPoint.split(\"[ ()]+\");\n" + "				double x = Double.parseDouble(split[1]);\n" + "				return x;\n" + "			}"),
	/**
	 *
	 */
	GETY("DBV_POINT3D_GETY", "Double", "String firstPoint", "			if (firstPoint == null) {\n" + "				return null;\n" + "			} else {\n" + "				String[] split = firstPoint.split(\"[ ()]+\");\n" + "				double y = Double.parseDouble(split[2]);\n" + "				return y;\n" + "			}"),
	/**
	 *
	 */
	GETZ("DBV_POINT3D_GETZ", "Double", "String firstPoint", "			if (firstPoint == null) {\n" + "				return null;\n" + "			} else {\n" + "				String[] split = firstPoint.split(\"[ ()]+\");\n" + "				double z = Double.parseDouble(split[3]);\n" + "				return z;\n" + "			}"),
	/**
	 *
	 */
	DIMENSION("DBV_POINT3D_GETDIMENSION", "Integer", "String firstPoint", "			return 0;"),
	/**
	 *
	 */
	BOUNDINGBOX("DBV_POINT3D_GETBOUNDINGBOX", "String", "String firstPoint", "			if (firstPoint == null) {\n" + "				return null;\n" + "			} else {\n" + "				String[] split = firstPoint.split(\"[ ()]+\");\n" + "				double x = Double.parseDouble(split[1]);\n" + "				double y = Double.parseDouble(split[2]);\n" + "				double z = Double.parseDouble(split[3]);\n" + "				String point = x+\" \"+y+\" \"+z;\n" + "				String resultString = \"POLYGON ((\"+point+\", \"+point+\", \"+point+\", \"+point+\", \"+point+\"))\";\n" + "				return resultString;\n" + "			}"),
	/**
	 *
	 */
	ASTEXT("DBV_POINT3D_ASTEXT", "String", "String firstPoint", "return firstPoint;");

	private final String functionName;
	private final String returnType;
	private final String parameters;
	private final String code;

	Point3DFunctions(String functionName, String returnType, String parameters, String code) {
		this.functionName = functionName;
		this.returnType = returnType;
		this.parameters = parameters;
		this.code = code;
	}

	@Override
	public String toString() {
		return functionName;
	}

	/**
	 *
	 * @param stmt
	 * @throws SQLException
	 */
	@Override
	public void add(Statement stmt) throws SQLException {
		stmt.execute("CREATE ALIAS IF NOT EXISTS " + functionName + " DETERMINISTIC AS $$ \n" + "@CODE " + returnType + " " + functionName + "(" + parameters + ") {\n" + code + "} $$;");
	}

	@Override
	public String alias() {
		return toString();
	}
}
