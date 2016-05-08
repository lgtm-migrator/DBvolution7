/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.gregs.dbvolution.exceptions;

import java.util.List;
import nz.co.gregs.dbvolution.DBRow;
import nz.co.gregs.dbvolution.datatypes.QueryableDatatype;
import nz.co.gregs.dbvolution.internal.properties.JavaProperty;
import nz.co.gregs.dbvolution.internal.properties.PropertyWrapperDefinition;
import nz.co.gregs.dbvolution.internal.properties.RowDefinitionClassWrapper;

/**
 *
 * @author gregorygraham
 */
public class SingularReferenceToMultiColumnPrimaryKeyException extends DBRuntimeException {

	private static final long serialVersionUID = 1L;

	public SingularReferenceToMultiColumnPrimaryKeyException(JavaProperty adaptee, RowDefinitionClassWrapper referencedClassWrapper, PropertyWrapperDefinition[] primaryKeys) {
		super("Property " + adaptee.qualifiedName() + " references class " + referencedClassWrapper.javaName()
				+ " using an implicit primary key reference, but the referenced class has " + primaryKeys.length
				+ " primary key columns. You must include explicit foreign column names.");
	}

	public SingularReferenceToMultiColumnPrimaryKeyException(DBRow aThis, List<QueryableDatatype<?>> primaryKeys) {
		throw new UnsupportedOperationException(" Class " + aThis.getClass().getCanonicalName()
				+ "has a " + primaryKeys.size() + "columns in it's primary key but an attempt has been made to reference the PK as a sinlge column.  Switch to a multi-column method to set the PK columns"); //To change body of generated methods, choose Tools | Templates.
	}

}
