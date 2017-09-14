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
package com.intellij.codeInsight.intention.impl

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.lang.jvm.JvmClass
import com.intellij.lang.jvm.JvmModifier
import com.intellij.lang.jvm.JvmModifiersOwner
import com.intellij.lang.jvm.actions.JvmElementActionsFactory
import com.intellij.lang.jvm.actions.MemberRequest
import com.intellij.openapi.components.ServiceManager
import com.intellij.psi.PsiElement
import org.jetbrains.uast.*
import com.intellij.codeInsight.intention.JvmCommonIntentionActionsFactory as UastJvmCommonIntentionActionsFactory
import com.intellij.codeInsight.intention.MethodInsertionInfo as UastMethodInsertionInfo

@Deprecated("to be removed in 2017.3")
class UastJvmElementFactory(
  val renderer: JavaElementRenderer,
  val materializer: JavaElementMaterializer
) : JvmElementActionsFactory() {

  private fun getUastFactory(target: JvmModifiersOwner) = (target as? PsiElement)?.language?.let {
    UastJvmCommonIntentionActionsFactory.forLanguage(it)
  }

  override fun createChangeModifierActions(target: JvmModifiersOwner, request: MemberRequest.Modifier): List<IntentionAction> =
    with(request) {
      listOfNotNull(
        getUastFactory(target)?.createChangeModifierAction(target.asUast<UDeclaration>(), renderer.render(modifier), shouldPresent))
    }

  override fun createAddConstructorActions(targetClass: JvmClass, request: MemberRequest.Constructor): List<IntentionAction> =
    with(request) {
      getUastFactory(targetClass)?.createAddCallableMemberActions(
        UastMethodInsertionInfo.Constructor(
          targetClass.asUast(),
          modifiers.map { renderer.render(it) },
          typeParameters.map { materializer.materialize(it) },
          parameters.map { it.asUast<UParameter>() }
        )
      ) ?: emptyList()
    }

  override fun createAddMethodActions(targetClass: JvmClass, request: MemberRequest.Method): List<IntentionAction> =
    with(request) {
      getUastFactory(targetClass)?.createAddCallableMemberActions(
        UastMethodInsertionInfo.Method(
          targetClass.asUast(),
          name,
          modifiers.map { renderer.render(it) },
          typeParameters.map { materializer.materialize(it) },
          materializer.materialize(returnType),
          parameters.map { it.asUast<UParameter>() },
          modifiers.contains(JvmModifier.ABSTRACT)
        )
      ) ?: emptyList()
    }

  override fun createAddPropertyActions(targetClass: JvmClass, request: MemberRequest.Property): List<IntentionAction> =
    with(request) {
      getUastFactory(targetClass)?.createAddBeanPropertyActions(targetClass.asUast<UClass>(), propertyName,
                                                                renderer.render(visibilityModifier),
                                                                materializer.materialize(propertyType), setterRequired,
                                                                getterRequired) ?: emptyList()

    }


}

@Deprecated("remove after kotlin plugin will be ported")
private inline fun <reified T : UElement> JvmModifiersOwner.asUast(): T = when (this) {
  is T -> this
  is PsiElement -> this.let {
    ServiceManager.getService(project, UastContext::class.java)
      .convertElement(this, null, T::class.java) as T?
  }
                   ?: throw UnsupportedOperationException("cant convert $this to ${T::class}")
  else -> throw UnsupportedOperationException("cant convert $this to ${T::class}")
}