package medusa

import org.specs2.Specification

object MacroSpec extends Specification {
  def is = s2"""
    Macro Spec
    ==========

    should be able to update Medusa          $updates
    should apply updates to Mutant           $toMutable
    should expose Mutant's setters in Medusa $exposeSetter
    should expose Mutant's getters in Medusa $exposeGetter
  """

  class MutantA
  def updates = {
    //given
    @medusa[MutantA] class Wrapper
    val w = new Wrapper(new MutantA)
    //when
    val w2 = w.update(x => println("hej"))
    //then
    (w.updates.size  must_== 0) and
    (w2.updates.size must_== 1)
  } 

  class MutantB {
    var s: String = ""
    var i: Int = 0

    def setS(ss: String): Unit = s = ss
    def setI(ii: Int): Unit    = i = ii
  }
  def toMutable = {
    //given
    @medusa[MutantB] class Wrapper
    //when
    val w = new Wrapper(new MutantB, List(x => x.setS("x"), x => x.setI(1)))
    val m = w.toMutable
    //then
    (m.s must_== "x") and
    (m.i must_== 1)
  }

  class MutantC {
    var s: String = ""
    var b: Boolean = false

    def setS(ss: String): Unit = s = ss
    def getS = s

    def setB: Unit = b = true
    def getB = b
  }

  def exposeSetter = {
    //given
    @medusa[MutantC] class Wrapper
    //when
    val w = new Wrapper(new MutantC)
    val m = w.setS("x").setB.toMutable
    //then
    val s = m.getS
    val b = m.getB
    
    (s must_== "x") and
    (b must_== true)
  }

  def exposeGetter = {
    //given
    @medusa[MutantC] class Wrapper
    //when
    val w = new Wrapper(new MutantC)
    //then
    val s = w.setS("x").getS

    s must_== "x"
  }
  

  def getType(x: Any) = {
    import scala.reflect.runtime.{universe => ru}
    val m = ru.runtimeMirror(x.getClass.getClassLoader)
    val im = m.reflect(p)
    val tpe = m.classSymbol(x.getClass).toType
    tpe
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
