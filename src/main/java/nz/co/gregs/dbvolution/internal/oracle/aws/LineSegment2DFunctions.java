/*
 * Copyright 2015 gregory.graham.
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
package nz.co.gregs.dbvolution.internal.oracle.aws;

import nz.co.gregs.dbvolution.internal.oracle.StringFunctions;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author gregory.graham
 */
public enum LineSegment2DFunctions {

	/**
	 *
	 */
	MAXY("NUMBER", "aLine VARCHAR", ""
			+ "   result       NUMBER;\n"
			+ "   coord        NUMBER;\n"
			+ "   pnt          VARCHAR (4000);\n"
			+ "   polyAsText   VARCHAR (4000);\n"
			+ "   textCoord    VARCHAR (4000);\n"
			+ "BEGIN\n"
			+ "   result := NULL;\n"
			+ "\n"
			+ "   IF aLine IS NOT NULL\n"
			+ "   THEN\n"
			+ "      polyAsText := aLine;\n"
			+ "      --DBMS_OUTPUT.PUT_LINE ('START: ' || polyAsText);\n"
			+ "      --'LINESTRING (2 3, 3 4, 4 5)'\n"
			+ "      polyAsText := " + StringFunctions.SUBSTRINGAFTER + "(polyAsText, '(');\n"
			+ "      polyAsText := " + StringFunctions.SUBSTRINGBEFORE + "(polyAsText, ')');\n"
			+ "\n"
			+ "      --'2 3, 3 4, 4 5'\n"
			+ "      --'3 4, 4 5'\n"
			+ "      --'4 5'\n"
			+ "      --NULL\n"
			+ "      WHILE polyAsText IS NOT NULL\n"
			+ "      LOOP\n"
			+ "         --DBMS_OUTPUT.PUT_LINE ('POLYASTEXT: ' || polyAsText);\n"
			+ "\n"
			+ "         textCoord := " + StringFunctions.SUBSTRINGBEFORE + " (polyAsText, ',');\n"
			+ "         --DBMS_OUTPUT.PUT_LINE ('TEXTCOORD: ' || textCoord);\n"
			+ "\n"
			+ "         IF textCoord IS NULL\n"
			+ "         THEN\n"
			+ "            textCoord := polyAsText;\n"
			+ "            --DBMS_OUTPUT.PUT_LINE ('TEXTCOORD: ' || textCoord);\n"
			+ "         --4 5\n"
			+ "         END IF;\n"
			+ "\n"
			+ "         --'2 3'\n"
			+ "         textCoord := " + StringFunctions.SUBSTRINGAFTER + " (textCoord, ' ');\n"
			+ "         --DBMS_OUTPUT.PUT_LINE ('TONUMBER: ' || textCoord);\n"
			+ "\n"
			+ "         --'3'\n"
			+ "\n"
			+ "         IF LENGTH (textCoord) > 0\n"
			+ "         THEN\n"
			+ "            coord := NULL;\n"
			+ "            coord := TO_NUMBER (textCoord);\n"
			+ "            --DBMS_OUTPUT.PUT_LINE ('COORD: ' || coord);\n"
			+ "\n"
			+ "            IF coord IS NOT NULL\n"
			+ "            THEN\n"
			+ "               IF result IS NULL OR result < coord\n"
			+ "               THEN\n"
			+ "                  result := coord;\n"
			+ "               END IF;\n"
			+ "            END IF;\n"
			+ "         END IF;\n"
			+ "\n"
			+ "         --DBMS_OUTPUT.PUT_LINE ('RESULT: ' || result);\n"
			+ "\n"
			+ "         polyAsText := " + StringFunctions.SUBSTRINGAFTER + " (polyAsText, ', ');\n"
			+ "         --DBMS_OUTPUT.PUT_LINE ('polyAsText: ' || polyAsText);\n"
			+ "      --'3 4, 4 5'\n"
			+ "      --'4 5'\n"
			+ "      --NULL\n"
			+ "      END LOOP;\n"
			+ "   END IF;\n"
			+ "\n"
			+ "   --   END IF;\n"
			+ "\n"
			+ "   RETURN result;\n"
			+ "END;"),

	/**
	 *
	 */
	MAXX("number", "aLine VARCHAR", ""
			+ "   result       NUMBER;\n"
			+ "   coord        NUMBER;\n"
			+ "   pnt          VARCHAR (4000);\n"
			+ "   polyAsText   VARCHAR (4000);\n"
			+ "   textCoord    VARCHAR (4000);\n"
			+ "BEGIN\n"
			+ "   result := NULL;\n"
			+ "\n"
			+ "   IF aLine IS NOT NULL\n"
			+ "   THEN\n"
			+ "      polyAsText := aLine;\n"
			+ "      --DBMS_OUTPUT.PUT_LINE ('START: ' || polyAsText);\n"
			+ "      --'LINESTRING (2 3, 3 4, 4 5)'\n"
			+ "      polyAsText := " + StringFunctions.SUBSTRINGAFTER + " (polyAsText, '(');\n"
			+ "      polyAsText := " + StringFunctions.SUBSTRINGBEFORE + " (polyAsText, ')');\n"
			+ "\n"
			+ "      --'2 3, 3 4, 4 5'\n"
			+ "      --'3 4, 4 5'\n"
			+ "      --'4 5'\n"
			+ "      --NULL\n"
			+ "      WHILE polyAsText IS NOT NULL\n"
			+ "      LOOP\n"
			+ "         --DBMS_OUTPUT.PUT_LINE ('POLYASTEXT: ' || polyAsText);\n"
			+ "\n"
			+ "         textCoord := " + StringFunctions.SUBSTRINGBEFORE + " (polyAsText, ',');\n"
			+ "         --DBMS_OUTPUT.PUT_LINE ('TEXTCOORD: ' || textCoord);\n"
			+ "\n"
			+ "         IF textCoord IS NULL\n"
			+ "         THEN\n"
			+ "            textCoord := polyAsText;\n"
			+ "            --DBMS_OUTPUT.PUT_LINE ('TEXTCOORD: ' || textCoord);\n"
			+ "         --4 5\n"
			+ "         END IF;\n"
			+ "\n"
			+ "         --'2 3'\n"
			+ "         textCoord := " + StringFunctions.SUBSTRINGBEFORE + " (textCoord, ' ');\n"
			+ "         --DBMS_OUTPUT.PUT_LINE ('TONUMBER: ' || textCoord);\n"
			+ "\n"
			+ "         --'3'\n"
			+ "\n"
			+ "         IF LENGTH (textCoord) > 0\n"
			+ "         THEN\n"
			+ "            coord := NULL;\n"
			+ "            coord := TO_NUMBER (textCoord);\n"
			+ "            --DBMS_OUTPUT.PUT_LINE ('COORD: ' || coord);\n"
			+ "\n"
			+ "            IF coord IS NOT NULL\n"
			+ "            THEN\n"
			+ "               IF result IS NULL OR result < coord\n"
			+ "               THEN\n"
			+ "                  result := coord;\n"
			+ "               END IF;\n"
			+ "            END IF;\n"
			+ "         END IF;\n"
			+ "\n"
			+ "         --DBMS_OUTPUT.PUT_LINE ('RESULT: ' || result);\n"
			+ "\n"
			+ "         polyAsText := " + StringFunctions.SUBSTRINGAFTER + " (polyAsText, ', ');\n"
			+ "         --DBMS_OUTPUT.PUT_LINE ('polyAsText: ' || polyAsText);\n"
			+ "      --'3 4, 4 5'\n"
			+ "      --'4 5'\n"
			+ "      --NULL\n"
			+ "      END LOOP;\n"
			+ "   END IF;\n"
			+ "\n"
			+ "   --   END IF;\n"
			+ "\n"
			+ "   RETURN result;\n"
			+ "END;"),

	/**
	 *
	 */
	MINX("NUMBER", "aLine VARCHAR", ""
			+ "   result       NUMBER;\n"
			+ "   coord        NUMBER;\n"
			+ "   pnt          VARCHAR (4000);\n"
			+ "   polyAsText   VARCHAR (4000);\n"
			+ "   textCoord    VARCHAR (4000);\n"
			+ "BEGIN\n"
			+ "   result := NULL;\n"
			+ "\n"
			+ "   IF aLine IS NOT NULL\n"
			+ "   THEN\n"
			+ "      polyAsText := aLine;\n"
			+ "      --DBMS_OUTPUT.PUT_LINE ('START: ' || polyAsText);\n"
			+ "      --'LINESTRING (2 3, 3 4, 4 5)'\n"
			+ "      polyAsText := " + StringFunctions.SUBSTRINGAFTER + " (polyAsText, '(');\n"
			+ "      polyAsText := " + StringFunctions.SUBSTRINGBEFORE + " (polyAsText, ')');\n"
			+ "\n"
			+ "      --'2 3, 3 4, 4 5'\n"
			+ "      --'3 4, 4 5'\n"
			+ "      --'4 5'\n"
			+ "      --NULL\n"
			+ "      WHILE polyAsText IS NOT NULL\n"
			+ "      LOOP\n"
			+ "         --DBMS_OUTPUT.PUT_LINE ('POLYASTEXT: ' || polyAsText);\n"
			+ "\n"
			+ "         textCoord := " + StringFunctions.SUBSTRINGBEFORE + " (polyAsText, ',');\n"
			+ "         --DBMS_OUTPUT.PUT_LINE ('TEXTCOORD: ' || textCoord);\n"
			+ "\n"
			+ "         IF textCoord IS NULL\n"
			+ "         THEN\n"
			+ "            textCoord := polyAsText;\n"
			+ "            --DBMS_OUTPUT.PUT_LINE ('TEXTCOORD: ' || textCoord);\n"
			+ "         --4 5\n"
			+ "         END IF;\n"
			+ "\n"
			+ "         --'2 3'\n"
			+ "         textCoord := " + StringFunctions.SUBSTRINGBEFORE + " (textCoord, ' ');\n"
			+ "         --DBMS_OUTPUT.PUT_LINE ('TONUMBER: ' || textCoord);\n"
			+ "\n"
			+ "         --'3'\n"
			+ "\n"
			+ "         IF LENGTH (textCoord) > 0\n"
			+ "         THEN\n"
			+ "            coord := NULL;\n"
			+ "            coord := TO_NUMBER (textCoord);\n"
			+ "            --DBMS_OUTPUT.PUT_LINE ('COORD: ' || coord);\n"
			+ "\n"
			+ "            IF coord IS NOT NULL\n"
			+ "            THEN\n"
			+ "               IF result IS NULL OR result > coord\n"
			+ "               THEN\n"
			+ "                  result := coord;\n"
			+ "               END IF;\n"
			+ "            END IF;\n"
			+ "         END IF;\n"
			+ "\n"
			+ "         --DBMS_OUTPUT.PUT_LINE ('RESULT: ' || result);\n"
			+ "\n"
			+ "         polyAsText := " + StringFunctions.SUBSTRINGAFTER + " (polyAsText, ', ');\n"
			+ "         --DBMS_OUTPUT.PUT_LINE ('polyAsText: ' || polyAsText);\n"
			+ "      --'3 4, 4 5'\n"
			+ "      --'4 5'\n"
			+ "      --NULL\n"
			+ "      END LOOP;\n"
			+ "   END IF;\n"
			+ "\n"
			+ "   --   END IF;\n"
			+ "\n"
			+ "   RETURN result;\n"
			+ "END;"),

	/**
	 *
	 */
	MINY("number", "aLine VARCHAR", ""
			+ "   result       NUMBER;\n"
			+ "   coord        NUMBER;\n"
			+ "   pnt          VARCHAR (4000);\n"
			+ "   polyAsText   VARCHAR (4000);\n"
			+ "   textCoord    VARCHAR (4000);\n"
			+ "BEGIN\n"
			+ "   result := NULL;\n"
			+ "\n"
			+ "   IF aLine IS NOT NULL\n"
			+ "   THEN\n"
			+ "      polyAsText := aLine;\n"
			+ "      --DBMS_OUTPUT.PUT_LINE ('START: ' || polyAsText);\n"
			+ "      --'LINESTRING (2 3, 3 4, 4 5)'\n"
			+ "      polyAsText := " + StringFunctions.SUBSTRINGAFTER + " (polyAsText, '(');\n"
			+ "      polyAsText := " + StringFunctions.SUBSTRINGBEFORE + " (polyAsText, ')');\n"
			+ "\n"
			+ "      --'2 3, 3 4, 4 5'\n"
			+ "      --'3 4, 4 5'\n"
			+ "      --'4 5'\n"
			+ "      --NULL\n"
			+ "      WHILE polyAsText IS NOT NULL\n"
			+ "      LOOP\n"
			+ "         --DBMS_OUTPUT.PUT_LINE ('POLYASTEXT: ' || polyAsText);\n"
			+ "\n"
			+ "         textCoord := " + StringFunctions.SUBSTRINGBEFORE + " (polyAsText, ',');\n"
			+ "         --DBMS_OUTPUT.PUT_LINE ('TEXTCOORD: ' || textCoord);\n"
			+ "\n"
			+ "         IF textCoord IS NULL\n"
			+ "         THEN\n"
			+ "            textCoord := polyAsText;\n"
			+ "            --DBMS_OUTPUT.PUT_LINE ('TEXTCOORD: ' || textCoord);\n"
			+ "         --4 5\n"
			+ "         END IF;\n"
			+ "\n"
			+ "         --'2 3'\n"
			+ "         textCoord := " + StringFunctions.SUBSTRINGAFTER + " (textCoord, ' ');\n"
			+ "         --DBMS_OUTPUT.PUT_LINE ('TONUMBER: ' || textCoord);\n"
			+ "\n"
			+ "         --'3'\n"
			+ "\n"
			+ "         IF LENGTH (textCoord) > 0\n"
			+ "         THEN\n"
			+ "            coord := NULL;\n"
			+ "            coord := TO_NUMBER (textCoord);\n"
			+ "            --DBMS_OUTPUT.PUT_LINE ('COORD: ' || coord);\n"
			+ "\n"
			+ "            IF coord IS NOT NULL\n"
			+ "            THEN\n"
			+ "               IF result IS NULL OR result > coord\n"
			+ "               THEN\n"
			+ "                  result := coord;\n"
			+ "               END IF;\n"
			+ "            END IF;\n"
			+ "         END IF;\n"
			+ "\n"
			+ "         --DBMS_OUTPUT.PUT_LINE ('RESULT: ' || result);\n"
			+ "\n"
			+ "         polyAsText := " + StringFunctions.SUBSTRINGAFTER + " (polyAsText, ', ');\n"
			+ "      --DBMS_OUTPUT.PUT_LINE ('polyAsText: ' || polyAsText);\n"
			+ "      --'3 4, 4 5'\n"
			+ "      --'4 5'\n"
			+ "      --NULL\n"
			+ "      END LOOP;\n"
			+ "   END IF;\n"
			+ "\n"
			+ "   --   END IF;\n"
			+ "\n"
			+ "   RETURN result;\n"
			+ "END;"),

	/**
	 *
	 */
	BOUNDINGBOX("VARCHAR", "lineString IN VARCHAR", ""
			+ "   result   VARCHAR (2002);\n"
			+ "   maxx     NUMBER;\n"
			+ "   minx     NUMBER;\n"
			+ "   maxy     NUMBER;\n"
			+ "   miny     NUMBER;\n"
			+ "BEGIN\n"
			+ "   IF lineString IS NULL\n"
			+ "   THEN\n"
			+ "      RETURN NULL;\n"
			+ "   ELSE\n"
			+ "      maxx := DBV_LINE2DFN_MAXX (lineString);\n"
			+ "      minx := DBV_LINE2DFN_MINX (lineString);\n"
			+ "      maxy := DBV_LINE2DFN_MAXY (lineString);\n"
			+ "      miny := DBV_LINE2DFN_MINY (lineString);\n"
			+ "      result := NULL;\n"
			+ "      result :=\n"
			+ "            'POLYGON (('\n"
			+ "         || minx\n"
			+ "         || ' '\n"
			+ "         || miny\n"
			+ "         || ', '\n"
			+ "         || maxx\n"
			+ "         || ' '\n"
			+ "         || miny\n"
			+ "         || ', '\n"
			+ "         || maxx\n"
			+ "         || ' '\n"
			+ "         || maxy\n"
			+ "         || ', '\n"
			+ "         || minx\n"
			+ "         || ' '\n"
			+ "         || maxy\n"
			+ "         || ', '\n"
			+ "         || minx\n"
			+ "         || ' '\n"
			+ "         || miny\n"
			+ "         || '))';\n"
			+ "      RETURN result;\n"
			+ "   END IF;\n"
			+ "END;"),

	/**
	 *
	 */
	INTERSECTS_LSEG2D("NUMBER", "firstLine IN VARCHAR, secondLine IN VARCHAR", "   p0x           NUMBER;\n"
			+ "   p0y           NUMBER;\n"
			+ "   p1x           NUMBER;\n"
			+ "   p1y           NUMBER;\n"
			+ "   p2x           NUMBER;\n"
			+ "   p2y           NUMBER;\n"
			+ "   p3x           NUMBER;\n"
			+ "   p3y           NUMBER;\n"
			+ "   s1_x          NUMBER;\n"
			+ "   s1_y          NUMBER;\n"
			+ "   s2_x          NUMBER;\n"
			+ "   s2_y          NUMBER;\n"
			+ "   s             NUMBER;\n"
			+ "   t             NUMBER;\n"
			+ "   polyAsText    VARCHAR (4000);\n"
			+ "   pointAsText   VARCHAR (4000);\n"
			+ "BEGIN\n"
			+ "   IF (firstLine IS NULL OR secondLine IS NULL)\n"
			+ "   THEN\n"
			+ "      RETURN NULL;\n"
			+ "   END IF;\n"
			+ "\n"
			+ "   --'LINESTRING (2 3, 3 4)'\n"
			+ "   polyAsText := firstLine;\n"
			+ "   polyAsText := " + StringFunctions.SUBSTRINGAFTER + " (polyAsText, '(');\n"
			+ "   polyAsText := " + StringFunctions.SUBSTRINGBEFORE + " (polyAsText, ')');\n"
			+ "   --'2 3, 3 4'\n"
			+ "   pointAsText := " + StringFunctions.SUBSTRINGBEFORE + " (polyAsText, ',');\n"
			+ "   --'2 3'\n"
			+ "   p0x := " + StringFunctions.SUBSTRINGBEFORE + " (pointAsText, ' ');\n"
			+ "   p0y := " + StringFunctions.SUBSTRINGAFTER + " (pointAsText, ' ');\n"
			+ "   pointAsText := " + StringFunctions.SUBSTRINGAFTER + " (polyAsText, ', ');\n"
			+ "   --'3 4'\n"
			+ "   p1x := " + StringFunctions.SUBSTRINGBEFORE + " (pointAsText, ' ');\n"
			+ "   p1y := " + StringFunctions.SUBSTRINGAFTER + " (pointAsText, ' ');\n"
			+ "\n"
			+ "   --'LINESTRING (2 3, 3 4)'\n"
			+ "   polyAsText := secondLine;\n"
			+ "   polyAsText := " + StringFunctions.SUBSTRINGAFTER + " (polyAsText, '(');\n"
			+ "   polyAsText := " + StringFunctions.SUBSTRINGBEFORE + " (polyAsText, ')');\n"
			+ "   --'2 3, 3 4'\n"
			+ "   pointAsText := " + StringFunctions.SUBSTRINGBEFORE + " (polyAsText, ',');\n"
			+ "   --'2 3'\n"
			+ "   p2x := " + StringFunctions.SUBSTRINGBEFORE + " (pointAsText, ' ');\n"
			+ "   p2y := " + StringFunctions.SUBSTRINGAFTER + " (pointAsText, ' ');\n"
			+ "   pointAsText := " + StringFunctions.SUBSTRINGAFTER + " (polyAsText, ', ');\n"
			+ "   --'3 4'\n"
			+ "   p3x := " + StringFunctions.SUBSTRINGBEFORE + " (pointAsText, ' ');\n"
			+ "   p3y := " + StringFunctions.SUBSTRINGAFTER + " (pointAsText, ' ');\n"
			+ "   --double s1_x, s1_y, s2_x, s2_y;\n"
			+ "   --double i_x, i_y;\n"
			+ "   s1_x := p1x - p0x;\n"
			+ "   s1_y := p1y - p0y;\n"
			+ "   s2_x := p3x - p2x;\n"
			+ "   s2_y := p3y - p2y;\n"
			+ "\n"
			+ "   --double s, t;\n"
			+ "\n"
			+ "   s :=\n"
			+ "        (-s1_y * (p0x - p2x) + s1_x * (p0y - p2y))\n"
			+ "      / (-s2_x * s1_y + s1_x * s2_y);\n"
			+ "   t :=\n"
			+ "        (s2_x * (p0y - p2y) - s2_y * (p0x - p2x))\n"
			+ "      / (-s2_x * s1_y + s1_x * s2_y);\n"
			+ "\n"
			+ "   IF (s >= 0 AND s <= 1 AND t >= 0 AND t <= 1)\n"
			+ "   THEN\n"
			+ "      -- Collision detected\n"
			+ "      --i_x = p0x + (t * s1_x);\n"
			+ "      --i_y = p0y + (t * s1_y);\n"
			+ "      RETURN 1;\n"
			+ "   ELSE                                                        -- No collision\n"
			+ "      RETURN 0;\n"
			+ "   END IF;\n"
			+ "END;"),

	/**
	 *
	 */
	EQUALS("NUMBER", "lineString1 IN VARCHAR, lineString2 IN VARCHAR", ""
			+ "BEGIN\n"
			+ "   RETURN CASE WHEN (lineString1 = lineString2) THEN 1 ELSE 0 END;\n"
			+ "END;"),

	/**
	 *
	 */
	INTERSECTPT_LSEG2D("VARCHAR", "firstLine IN VARCHAR, secondLine IN VARCHAR", ""
			+ "   p0x           NUMBER;\n"
			+ "   p0y           NUMBER;\n"
			+ "   p1x           NUMBER;\n"
			+ "   p1y           NUMBER;\n"
			+ "   p2x           NUMBER;\n"
			+ "   p2y           NUMBER;\n"
			+ "   p3x           NUMBER;\n"
			+ "   p3y           NUMBER;\n"
			+ "   s1_x          NUMBER;\n"
			+ "   s1_y          NUMBER;\n"
			+ "   s2_x          NUMBER;\n"
			+ "   s2_y          NUMBER;\n"
			+ "   s             NUMBER;\n"
			+ "   t             NUMBER;\n"
			+ "   polyAsText    VARCHAR (4000);\n"
			+ "   pointAsText   VARCHAR (4000);\n"
			+ "   i_x           NUMBER;\n"
			+ "   i_y           NUMBER;\n"
			+ "BEGIN\n"
			+ "   IF (firstLine IS NULL OR secondLine IS NULL)\n"
			+ "   THEN\n"
			+ "      RETURN NULL;\n"
			+ "   END IF;\n"
			+ "\n"
			+ "   --'LINESTRING (2 3, 3 4)'\n"
			+ "   polyAsText := firstLine;\n"
			+ "   polyAsText := DBV_STRINGFN_SUBSTRINGAFTER (polyAsText, '(');\n"
			+ "   polyAsText := DBV_STRINGFN_SUBSTRINGBEFORE (polyAsText, ')');\n"
			+ "   --'2 3, 3 4'\n"
			+ "   pointAsText := DBV_STRINGFN_SUBSTRINGBEFORE (polyAsText, ',');\n"
			+ "   --'2 3'\n"
			+ "   p0x := DBV_STRINGFN_SUBSTRINGBEFORE (pointAsText, ' ');\n"
			+ "   p0y := DBV_STRINGFN_SUBSTRINGAFTER (pointAsText, ' ');\n"
			+ "   pointAsText := DBV_STRINGFN_SUBSTRINGAFTER (polyAsText, ', ');\n"
			+ "   --'3 4'\n"
			+ "   p1x := DBV_STRINGFN_SUBSTRINGBEFORE (pointAsText, ' ');\n"
			+ "   p1y := DBV_STRINGFN_SUBSTRINGAFTER (pointAsText, ' ');\n"
			+ "\n"
			+ "   --'LINESTRING (2 3, 3 4)'\n"
			+ "   polyAsText := secondLine;\n"
			+ "   polyAsText := DBV_STRINGFN_SUBSTRINGAFTER (polyAsText, '(');\n"
			+ "   polyAsText := DBV_STRINGFN_SUBSTRINGBEFORE (polyAsText, ')');\n"
			+ "   --'2 3, 3 4'\n"
			+ "   pointAsText := DBV_STRINGFN_SUBSTRINGBEFORE (polyAsText, ',');\n"
			+ "   --'2 3'\n"
			+ "   p2x := DBV_STRINGFN_SUBSTRINGBEFORE (pointAsText, ' ');\n"
			+ "   p2y := DBV_STRINGFN_SUBSTRINGAFTER (pointAsText, ' ');\n"
			+ "   pointAsText := DBV_STRINGFN_SUBSTRINGAFTER (polyAsText, ', ');\n"
			+ "   --'3 4'\n"
			+ "   p3x := DBV_STRINGFN_SUBSTRINGBEFORE (pointAsText, ' ');\n"
			+ "   p3y := DBV_STRINGFN_SUBSTRINGAFTER (pointAsText, ' ');\n"
			+ "   --double s1_x, s1_y, s2_x, s2_y;\n"
			+ "   --double i_x, i_y;\n"
			+ "   s1_x := p1x - p0x;\n"
			+ "   s1_y := p1y - p0y;\n"
			+ "   s2_x := p3x - p2x;\n"
			+ "   s2_y := p3y - p2y;\n"
			+ "\n"
			+ "   --double s, t;\n"
			+ "\n"
			+ "   s :=\n"
			+ "        (-s1_y * (p0x - p2x) + s1_x * (p0y - p2y))\n"
			+ "      / (-s2_x * s1_y + s1_x * s2_y);\n"
			+ "   t :=\n"
			+ "        (s2_x * (p0y - p2y) - s2_y * (p0x - p2x))\n"
			+ "      / (-s2_x * s1_y + s1_x * s2_y);\n"
			+ "\n"
			+ "   IF (s >= 0 AND s <= 1 AND t >= 0 AND t <= 1)\n"
			+ "   THEN\n"
			+ "      -- Collision detected\n"
			+ "      i_x := p0x + (t * s1_x);\n"
			+ "      i_y := p0y + (t * s1_y);\n"
			+ "      RETURN 'POINT (' || i_x || ' ' || i_y || ')';\n"
			+ "   ELSE                                                        -- No collision\n"
			+ "      RETURN NULL;\n"
			+ "   END IF;\n"
			+ "END;");

	private final String returnType;
	private final String parameters;
	private final String code;

	LineSegment2DFunctions(String returnType, String parameters, String code) {
		this.returnType = returnType;
		this.parameters = parameters;
		this.code = code;
	}

	@Override
	public String toString() {
		return "DBV_LSEG2D_" + name();
	}

	/**
	 *
	 * @param stmt
	 * @throws SQLException
	 */
	public void add(Statement stmt) throws SQLException {
		try {
			if (!this.code.isEmpty()) {
				final String createFn = "CREATE OR REPLACE FUNCTION " + this + "(" + this.parameters + ")\n"
						+ "    RETURN " + this.returnType
						+ " AS \n" + "\n" + this.code;
				System.out.println(createFn);
				stmt.execute(createFn);
			}
		} catch (SQLException ex) {
			;
		}
	}
}
