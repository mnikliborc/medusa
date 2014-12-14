package medusa

import org.specs2.Specification

object MacroSpec extends Specification {
  def is = s2"""
    Macro Spec
    ==========

    should be able to create Medusa $creates
    should apply updates to Mutant $toMutant
  """

  def creates = {
    //given
    class Mutant
    @medusa[Mutant] class Wrapper
    val w = new Wrapper(new Mutant)
    //when
    val w2 = w.create(List(x => println("hej")))
    //then
    (w.updates.size  must_== 0) and
    (w2.updates.size must_== 1)
  }

  def toMutant = {
    //given
    class Mutant {
      var s: String = ""
      var i: Int = 0
  
      def setS(ss: String): Unit = s = ss
      def setI(ii: Int): Unit    = i = ii
    }
  
    @medusa[Mutant] class Wrapper
    //when
    val w = new Wrapper(new Mutant, List(x => x.setS("x"), x => x.setI(1)))
    val m = w.toMutant
    //then
    (m.s must_== "x") and
    (m.i must_== 1)
  }

  // def passByName = {
  //   //given
  //   object Mutant { var mutantCreated = false }
  //   class Mutant { Mutant.mutantCreated = true }
  //   @medusa[Mutant] class Wrapper

  //   //when then
  //   val w = new Wrapper(new Mutant)

  //   Mutant.mutantCreated must_== false
  // }
}
