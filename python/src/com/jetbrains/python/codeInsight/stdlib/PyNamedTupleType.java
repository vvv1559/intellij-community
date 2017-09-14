/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jetbrains.python.codeInsight.stdlib;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.python.psi.PyCallSiteExpression;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyExpression;
import com.jetbrains.python.psi.types.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author yole
 */
public class PyNamedTupleType extends PyClassTypeImpl implements PyCallableType {

  @NotNull
  private final PsiElement myDeclaration;

  @NotNull
  private final String myName;

  @NotNull
  private final Map<String, FieldTypeAndDefaultValue> myFields;

  @NotNull
  private final DefinitionLevel myDefinitionLevel;

  public PyNamedTupleType(@NotNull PyClass tupleClass,
                          @NotNull PsiElement declaration,
                          @NotNull String name,
                          @NotNull Map<String, FieldTypeAndDefaultValue> fields,
                          @NotNull DefinitionLevel definitionLevel) {
    super(tupleClass, definitionLevel != DefinitionLevel.INSTANCE);
    myDeclaration = declaration;
    myFields = Collections.unmodifiableMap(fields);
    myName = name;
    myDefinitionLevel = definitionLevel;
  }

  @Override
  public Object[] getCompletionVariants(String completionPrefix, PsiElement location, ProcessingContext context) {
    final List<Object> result = new ArrayList<>();
    Collections.addAll(result, super.getCompletionVariants(completionPrefix, location, context));
    for (String field : myFields.keySet()) {
      result.add(LookupElementBuilder.create(field));
    }
    return ArrayUtil.toObjectArray(result);
  }

  @NotNull
  @Override
  public String getName() {
    return myName;
  }

  @Override
  public boolean isBuiltin() {
    return false;
  }

  @Nullable
  @Override
  public PyType getCallType(@NotNull TypeEvalContext context, @NotNull PyCallSiteExpression callSite) {
    if (myDefinitionLevel == DefinitionLevel.NT_FUNCTION) {
      return new PyNamedTupleType(myClass, myDeclaration, myName, myFields, DefinitionLevel.NEW_TYPE);
    }
    else if (myDefinitionLevel == DefinitionLevel.NEW_TYPE) {
      return new PyNamedTupleType(myClass, myDeclaration, myName, myFields, DefinitionLevel.INSTANCE);
    }

    return null;
  }

  @NotNull
  @Override
  public PyClassType toInstance() {
    return myDefinitionLevel == DefinitionLevel.NEW_TYPE
           ? new PyNamedTupleType(myClass, myDeclaration, myName, myFields, DefinitionLevel.INSTANCE)
           : this;
  }

  @NotNull
  @Override
  public PyClassLikeType toClass() {
    return myDefinitionLevel == DefinitionLevel.INSTANCE
           ? this
           : new PyNamedTupleType(myClass, myDeclaration, myName, myFields, DefinitionLevel.NEW_TYPE);
  }

  @Override
  public String toString() {
    return "PyNamedTupleType: " + myName;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    final PyNamedTupleType type = (PyNamedTupleType)o;
    return Objects.equals(myName, type.myName) &&
           Objects.equals(myFields.keySet(), type.myFields.keySet()) &&
           myDefinitionLevel == type.myDefinitionLevel;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), myName, myFields.keySet(), myDefinitionLevel);
  }

  @NotNull
  @Override
  public Set<String> getMemberNames(boolean inherited, @NotNull TypeEvalContext context) {
    final Set<String> result = super.getMemberNames(inherited, context);
    result.addAll(myFields.keySet());

    return result;
  }

  public int getElementCount() {
    return myFields.size();
  }

  @NotNull
  public Map<String, FieldTypeAndDefaultValue> getFields() {
    return myFields;
  }

  @Override
  public boolean isCallable() {
    return myDefinitionLevel != DefinitionLevel.INSTANCE;
  }

  @Nullable
  @Override
  public List<PyCallableParameter> getParameters(@NotNull TypeEvalContext context) {
    return isCallable()
           ? ContainerUtil.map(myFields.entrySet(), field -> fieldToCallableParameter(field.getKey(), field.getValue()))
           : null;
  }

  @NotNull
  private static PyCallableParameter fieldToCallableParameter(@NotNull String name, @NotNull FieldTypeAndDefaultValue typeAndDefaultValue) {
    return PyCallableParameterImpl.nonPsi(name, typeAndDefaultValue.getType(), typeAndDefaultValue.getDefaultValue());
  }

  public enum DefinitionLevel {

    NT_FUNCTION, // type for collections.namedtuple and typing.NamedTuple.__init__
    NEW_TYPE,
    INSTANCE
  }

  public static class FieldTypeAndDefaultValue {

    @Nullable
    private final PyType myType;

    @Nullable
    private final PyExpression myDefaultValue;

    public FieldTypeAndDefaultValue(@Nullable PyType type, @Nullable PyExpression defaultValue) {
      myType = type;
      myDefaultValue = defaultValue;
    }

    @Nullable
    public PyType getType() {
      return myType;
    }

    @Nullable
    public PyExpression getDefaultValue() {
      return myDefaultValue;
    }
  }
}
