/*
 * Copyright 2014 gregorygraham.
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
package nz.co.gregs.dbvolution.datatypes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import nz.co.gregs.dbvolution.DBRow;
import nz.co.gregs.dbvolution.expressions.NumberResult;
import nz.co.gregs.dbvolution.operators.DBPermittedRangeExclusiveOperator;
import nz.co.gregs.dbvolution.operators.DBPermittedRangeInclusiveOperator;
import nz.co.gregs.dbvolution.operators.DBPermittedRangeOperator;
import nz.co.gregs.dbvolution.operators.DBPermittedValuesOperator;

/**
 * Like {@link DBInteger} except that the database value can be easily
 * interpreted as an enumeration with {@code Integer} or {@code Long} codes.
 *
 * @param <E> type of enumeration class
 */
public class DBIntegerEnum<E extends Enum<E> & DBEnumValue<? extends Number>> extends DBEnum<E> {

    private static final long serialVersionUID = 1L;

    public DBIntegerEnum() {
    }

    public DBIntegerEnum(Integer value) {
        super(value.longValue());
    }

    public DBIntegerEnum(Long value) {
        super(value);
    }

    public DBIntegerEnum(NumberResult numberExpression) {
        super(numberExpression);
    }

    public DBIntegerEnum(E value) {
        super(value);
    }

    @Override
    public void setValue(Object newLiteralValue) {
        if (newLiteralValue instanceof Long) {
            setValue((Long) newLiteralValue);
        } else if (newLiteralValue instanceof Integer) {
            setValue((Integer) newLiteralValue);
        } else if (newLiteralValue instanceof DBIntegerEnum) {
            setValue(((DBIntegerEnum) newLiteralValue).literalValue);
        } else {
            throw new ClassCastException(this.getClass().getSimpleName() + ".setValue() Called With A Non-Long: Use only Long with this class");
        }
    }

    public void setValue(Long newLiteralValue) {
        super.setLiteralValue(newLiteralValue);
    }

    public void setValue(Integer newLiteralValue) {
        super.setLiteralValue(newLiteralValue);
    }

    @Override
    protected void validateLiteralValue(E enumValue) {
        Object literalValue = enumValue.getCode();
        if (literalValue != null) {
            if (!(literalValue instanceof Integer || literalValue instanceof Long)) {
                String enumMethodRef = enumValue.getClass().getName() + "." + enumValue.name() + ".getLiteralValue()";
                String literalValueTypeRef = literalValue.getClass().getName();
                throw new IncompatibleClassChangeError("Enum literal type is not valid: "
                        + enumMethodRef + " returned a " + literalValueTypeRef + ", which is not valid for a " + this.getClass().getSimpleName());
            }
        }
    }

    @Override
    public String getSQLDatatype() {
        return new DBInteger().getSQLDatatype();
    }

    @Override
    public void setFromResultSet(ResultSet resultSet, String fullColumnName) {
        blankQuery();
        if (resultSet == null || fullColumnName == null) {
            this.setToNull();
        } else {
            Long dbValue;
            try {
                dbValue = resultSet.getLong(fullColumnName);
                if (resultSet.wasNull()) {
                    dbValue = null;
                }
            } catch (SQLException ex) {
                dbValue = null;
            }
            if (dbValue == null) {
                this.setToNull();
            } else {
                this.setLiteralValue(dbValue);
            }
        }
        setUnchanged();
        setDefined(true);
    }

    @Override
    public DBInteger getQueryableDatatypeForExpressionValue() {
        return new DBInteger();
    }

    @Override
    public boolean isAggregator() {
        return false;
    }

    @Override
    public Set<DBRow> getTablesInvolved() {
        return new HashSet<DBRow>();
    }

    /**
     *
     * reduces the rows to only the object, Set, List, Array, or vararg of
     * objects
     *
     * @param permitted
     */
    public void permittedValues(E... permitted) {
        this.setOperator(new DBPermittedValuesOperator(convertToLiteral(permitted)));
    }
    
    /**
     *
     * excludes the object, Set, List, Array, or vararg of objects
     *
     *
     * @param excluded
     */
    public void excludedValues(E... excluded) {
        this.setOperator(new DBPermittedValuesOperator(convertToLiteral(excluded)));
        negateOperator();
    }

    /**
     * Performs searches based on a range.
     *
     * if both ends of the range are specified the lower-bound will be included
     * in the search and the upper-bound excluded. I.e permittedRange(1,3) will
     * return 1 and 2.
     *
     * <p>
     * if the upper-bound is null the range will be open ended and inclusive.
     * <br>
     * I.e permittedRange(1,null) will return 1,2,3,4,5, etc.
     *
     * <p>
     * if the upper-bound is null the range will be open ended and exclusive.
     * <br>
     * I.e permittedRange(null, 5) will return 4,3,2,1, etc.
     *
     * @param lowerBound
     * @param upperBound
     */
    public void permittedRange(E lowerBound, E upperBound) {
        setOperator(new DBPermittedRangeOperator(convertToLiteral(lowerBound), convertToLiteral(upperBound)));
    }

    /**
     * Performs searches based on a range.
     *
     * if both ends of the range are specified both the lower- and upper-bound
     * will be included in the search. I.e permittedRangeInclusive(1,3) will
     * return 1, 2, and 3.
     *
     * <p>
     * if the upper-bound is null the range will be open ended and inclusive.
     * <br>
     * I.e permittedRangeInclusive(1,null) will return 1,2,3,4,5, etc.
     *
     * <p>
     * if the upper-bound is null the range will be open ended and inclusive.
     * <br>
     * I.e permittedRangeInclusive(null, 5) will return 5,4,3,2,1, etc.
     *
     * @param lowerBound
     * @param upperBound
     */
    public void permittedRangeInclusive(E lowerBound, E upperBound) {
        setOperator(new DBPermittedRangeInclusiveOperator(convertToLiteral(lowerBound), convertToLiteral(upperBound)));
    }

    /**
     * Performs searches based on a range.
     *
     * if both ends of the range are specified both the lower- and upper-bound
     * will be excluded in the search. I.e permittedRangeExclusive(1,3) will
     * return 2.
     *
     * <p>
     * if the upper-bound is null the range will be open ended and exclusive.
     * <br>
     * I.e permittedRangeExclusive(1,null) will return 2,3,4,5, etc.
     *
     * <p>
     * if the upper-bound is null the range will be open ended and exclusive.
     * <br>
     * I.e permittedRangeExclusive(null, 5) will return 4,3,2,1, etc.
     *
     * @param lowerBound
     * @param upperBound
     */
    public void permittedRangeExclusive(E lowerBound, E upperBound) {
        setOperator(new DBPermittedRangeExclusiveOperator(convertToLiteral(lowerBound), convertToLiteral(upperBound)));
    }

    public void excludedRange(E lowerBound, E upperBound) {
        setOperator(new DBPermittedRangeOperator(convertToLiteral(lowerBound), convertToLiteral(upperBound)));
        negateOperator();
    }

    public void excludedRangeInclusive(E lowerBound, E upperBound) {
        setOperator(new DBPermittedRangeInclusiveOperator(convertToLiteral(lowerBound), convertToLiteral(upperBound)));
        negateOperator();
    }

    public void excludedRangeExclusive(E lowerBound, E upperBound) {
        setOperator(new DBPermittedRangeExclusiveOperator(convertToLiteral(lowerBound), convertToLiteral(upperBound)));
        negateOperator();
    }

    /**
     *
     * reduces the rows to only the object, Set, List, Array, or vararg of
     * objects
     *
     * @param permitted
     */
    public void permittedValues(Long... permitted) {
        this.setOperator(new DBPermittedValuesOperator((Object[])permitted));
    }
    
    /**
     *
     * excludes the object, Set, List, Array, or vararg of objects
     *
     *
     * @param excluded
     */
    public void excludedValues(Long... excluded) {
        this.setOperator(new DBPermittedValuesOperator((Object[])excluded));
        negateOperator();
    }

    /**
     * Performs searches based on a range.
     *
     * if both ends of the range are specified the lower-bound will be included
     * in the search and the upper-bound excluded. I.e permittedRange(1,3) will
     * return 1 and 2.
     *
     * <p>
     * if the upper-bound is null the range will be open ended and inclusive.
     * <br>
     * I.e permittedRange(1,null) will return 1,2,3,4,5, etc.
     *
     * <p>
     * if the upper-bound is null the range will be open ended and exclusive.
     * <br>
     * I.e permittedRange(null, 5) will return 4,3,2,1, etc.
     *
     * @param lowerBound
     * @param upperBound
     */
    public void permittedRange(Long lowerBound, Long upperBound) {
        setOperator(new DBPermittedRangeOperator(lowerBound, upperBound));
    }

    /**
     * Performs searches based on a range.
     *
     * if both ends of the range are specified both the lower- and upper-bound
     * will be included in the search. I.e permittedRangeInclusive(1,3) will
     * return 1, 2, and 3.
     *
     * <p>
     * if the upper-bound is null the range will be open ended and inclusive.
     * <br>
     * I.e permittedRangeInclusive(1,null) will return 1,2,3,4,5, etc.
     *
     * <p>
     * if the upper-bound is null the range will be open ended and inclusive.
     * <br>
     * I.e permittedRangeInclusive(null, 5) will return 5,4,3,2,1, etc.
     *
     * @param lowerBound
     * @param upperBound
     */
    public void permittedRangeInclusive(Long lowerBound, Long upperBound) {
        setOperator(new DBPermittedRangeInclusiveOperator(lowerBound, upperBound));
    }

    /**
     * Performs searches based on a range.
     *
     * if both ends of the range are specified both the lower- and upper-bound
     * will be excluded in the search. I.e permittedRangeExclusive(1,3) will
     * return 2.
     *
     * <p>
     * if the upper-bound is null the range will be open ended and exclusive.
     * <br>
     * I.e permittedRangeExclusive(1,null) will return 2,3,4,5, etc.
     *
     * <p>
     * if the upper-bound is null the range will be open ended and exclusive.
     * <br>
     * I.e permittedRangeExclusive(null, 5) will return 4,3,2,1, etc.
     *
     * @param lowerBound
     * @param upperBound
     */
    public void permittedRangeExclusive(Long lowerBound, Long upperBound) {
        setOperator(new DBPermittedRangeExclusiveOperator(lowerBound, upperBound));
    }

    public void excludedRange(Long lowerBound, Long upperBound) {
        setOperator(new DBPermittedRangeOperator(lowerBound, upperBound));
        negateOperator();
    }

    public void excludedRangeInclusive(Long lowerBound, Long upperBound) {
        setOperator(new DBPermittedRangeInclusiveOperator(lowerBound, upperBound));
        negateOperator();
    }

    public void excludedRangeExclusive(Long lowerBound, Long upperBound) {
        setOperator(new DBPermittedRangeExclusiveOperator(lowerBound, upperBound));
        negateOperator();
    }

    /**
     *
     * reduces the rows to only the object, Set, List, Array, or vararg of
     * objects
     *
     * @param permitted
     */
    public void permittedValues(Integer... permitted) {
        this.setOperator(new DBPermittedValuesOperator((Object[])permitted));
    }
    
    /**
     *
     * excludes the object, Set, List, Array, or vararg of objects
     *
     *
     * @param excluded
     */
    public void excludedValues(Integer... excluded) {
        this.setOperator(new DBPermittedValuesOperator((Object[])excluded));
        negateOperator();
    }

    /**
     * Performs searches based on a range.
     *
     * if both ends of the range are specified the lower-bound will be included
     * in the search and the upper-bound excluded. I.e permittedRange(1,3) will
     * return 1 and 2.
     *
     * <p>
     * if the upper-bound is null the range will be open ended and inclusive.
     * <br>
     * I.e permittedRange(1,null) will return 1,2,3,4,5, etc.
     *
     * <p>
     * if the upper-bound is null the range will be open ended and exclusive.
     * <br>
     * I.e permittedRange(null, 5) will return 4,3,2,1, etc.
     *
     * @param lowerBound
     * @param upperBound
     */
    public void permittedRange(Integer lowerBound, Integer upperBound) {
        setOperator(new DBPermittedRangeOperator(lowerBound, upperBound));
    }

    /**
     * Performs searches based on a range.
     *
     * if both ends of the range are specified both the lower- and upper-bound
     * will be included in the search. I.e permittedRangeInclusive(1,3) will
     * return 1, 2, and 3.
     *
     * <p>
     * if the upper-bound is null the range will be open ended and inclusive.
     * <br>
     * I.e permittedRangeInclusive(1,null) will return 1,2,3,4,5, etc.
     *
     * <p>
     * if the upper-bound is null the range will be open ended and inclusive.
     * <br>
     * I.e permittedRangeInclusive(null, 5) will return 5,4,3,2,1, etc.
     *
     * @param lowerBound
     * @param upperBound
     */
    public void permittedRangeInclusive(Integer lowerBound, Integer upperBound) {
        setOperator(new DBPermittedRangeInclusiveOperator(lowerBound, upperBound));
    }

    /**
     * Performs searches based on a range.
     *
     * if both ends of the range are specified both the lower- and upper-bound
     * will be excluded in the search. I.e permittedRangeExclusive(1,3) will
     * return 2.
     *
     * <p>
     * if the upper-bound is null the range will be open ended and exclusive.
     * <br>
     * I.e permittedRangeExclusive(1,null) will return 2,3,4,5, etc.
     *
     * <p>
     * if the upper-bound is null the range will be open ended and exclusive.
     * <br>
     * I.e permittedRangeExclusive(null, 5) will return 4,3,2,1, etc.
     *
     * @param lowerBound
     * @param upperBound
     */
    public void permittedRangeExclusive(Integer lowerBound, Integer upperBound) {
        setOperator(new DBPermittedRangeExclusiveOperator(lowerBound, upperBound));
    }

    public void excludedRange(Integer lowerBound, Integer upperBound) {
        setOperator(new DBPermittedRangeOperator(lowerBound, upperBound));
        negateOperator();
    }

    public void excludedRangeInclusive(Integer lowerBound, Integer upperBound) {
        setOperator(new DBPermittedRangeInclusiveOperator(lowerBound, upperBound));
        negateOperator();
    }

    public void excludedRangeExclusive(Integer lowerBound, Integer upperBound) {
        setOperator(new DBPermittedRangeExclusiveOperator(lowerBound, upperBound));
        negateOperator();
    }
}
