package scala.lms
package common

import lms.verify.VerifyOps

import scala.reflect.SourceContext

trait LiftVariablesIso extends LiftVariables with VerifyOps {
  this: Variables =>

  // Ambiguous
  def __newVar[T](init: T)(implicit o: Overloaded3, mIso: Iso1[T], pos: SourceContext): Var[mIso.G]
  = var_new(mIso.toRep(init))(mIso.typ, pos)
  // def newVarIso[T:Iso1](init: T)(implicit pos: SourceContext) = implicitly[Iso1[T]].toRep(init)

  def __assign[T](lhs: Var[Iso1[T].G], rhs: T)(implicit o: Overloaded3, mIso: Iso1[T], pos: SourceContext)
  = var_assign(lhs, mIso.toRep(rhs))(mIso.typ, pos)
}

