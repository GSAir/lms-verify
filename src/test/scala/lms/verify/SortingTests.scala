package lms.verify

class SortingTests extends TestSuite {
  val under = "srt"

  trait Sorting extends Dsl with DataOps { dsl =>
    def key[T:Iso] = implicitly[Iso[T]].id
    class Vec[T:Iso](val a: Pointer[T], val n: Rep[Int]) {
      def apply(i: Rep[Int]) = a(i)
      def valid = n==0 || (n>0 && a.valid(0 until n))
      def length = n
    }
    implicit def vecIso[T:Iso](implicit ev: Inv[Vec[T]]) = isodata[Vec[T],(Pointer[T],Rep[Int])](
      "vec_" + key[T],
      {x: Vec[T] => (x.a, x.n)},
      {x: (Pointer[T],Rep[Int]) => new Vec(x._1, x._2)}
    )
    implicit def vecInv[T:Inv] = invariant[Vec[T]] { x =>
      x.valid && ((0 until x.n) forall {i => x(i).check})
    }
    implicit def vecEq[T:Eq:Iso] = equality[Vec[T]] { (x, y) =>
      x.n == y.n && ((0 until x.n) forall {i => x(i) deep_equal y(i)})
    }
    def infix_separated[T:Iso](v: Vec[T]): Rep[Boolean] = {
      val (x, n) = (v.a, v.n)
      val pn: Int = x.p.size
      and_list((for (i <- 0 until pn: Range; j <- (i+1) until pn: Range) yield {
        val (a01,(m01,t01)) = x.pmt(i)
        val (a02,(m02,t02)) = x.pmt(j)
        implicit val t1 = t01.asInstanceOf[Typ[m01.T]]
        val a1 = a01.asInstanceOf[Rep[Array[m01.T]]]
        implicit val t2 = t02.asInstanceOf[Typ[m02.T]]
        val a2 = a02.asInstanceOf[Rep[Array[m02.T]]]
        forall{i1: Rep[Int] => forall{i2: Rep[Int] =>
          (0 <= i1 && i1 < n && 0 <= i2 && i2 < n) ==>
          separated(a1, i1, a2, i2)
        }}}).toList)
    }
    def infix_reflectMutable[T:Iso](v: Vec[T]): Unit = {
      requires(v.separated)
      ensures{result: Rep[Unit] => v.separated}
      v.a.reflectMutableInput
      v.a.assigns(0 until v.n)
    }

    def add_closure_cases[B:Iso](p: ((Lc,Lc)) => B => Rep[Boolean]) = {
      add_case[Lc1,B]("refl", { ls => v =>
        p((ls._1,ls._1))(v)
      })
      add_case[(Lc,Lc),B]("sym", { ls => v =>
        (p((ls._1, ls._2))(v)) ==> (p((ls._2, ls._1))(v))
      })
      add_case[(Lc,Lc,Lc),B]("trans", {ls => v =>
        ((p((ls._1, ls._2))(v)) && (p((ls._2, ls._3))(v))) ==>
        (p((ls._1, ls._3))(v))
      })
    }

    def reflexiveTransitiveClosure[B:Iso](p1: ((Lc,Lc)) => B => Rep[Boolean],
      as: String = "Rel") = inductive[(Lc,Lc),B](
      implicitly[Iso[B]].id+"_"+as, { p =>
        add_closure_cases(p)
        add_case[(Lc,Lc),B]("step", { ls => v => p1(ls)(v) })
      })


    def Swapped_deep_eq[T:Iso:Eq](ls: (Lc,Lc))(a: Vec[T], i: Rep[Int], j: Rep[Int]) = {
      val (l1, l2) = ls
      // NOTE: work-around for nested vectors
      // since deep_equal might do reads, we need to tell frama-c
      // which state to use for those, because inductive definitions
      // do not have the default state Here
      // it should not matter, since we are not modifying elements (parametricity)
      // still, maybe deep equality is not the right concept here?
      def w(x: => Rep[Boolean]) = at(x, l1) && at(x, l2)
      w((at(a(i),l1)) deep_equal (at(a(j),l2))) &&
      w((at(a(j),l1)) deep_equal (at(a(i),l2))) &&
      forall{k: Rep[Int] =>
        (0 <= k && k < a.length && k != i && k != j) ==>
        w((at(a(k),l1)) deep_equal (at(a(k),l2)))}
    }
    // shallow_equal works just as well:
    def Swapped_shallow_eq[T:Iso](ls: (Lc,Lc))(a: Vec[T], i: Rep[Int], j: Rep[Int]) = {
      val (l1, l2) = ls
      ((at(a(i),l1)) shallow_equal (at(a(j),l2))) &&
      ((at(a(j),l1)) shallow_equal (at(a(i),l2))) &&
      forall{k: Rep[Int] =>
        (0 <= k && k < a.length && k != i && k != j) ==>
        ((at(a(k),l1)) shallow_equal (at(a(k),l2)))}
    }
    def Swapped[T:Iso:Eq](ls: (Lc,Lc))(a: Vec[T], i: Rep[Int], j: Rep[Int]) =
      Swapped_deep_eq(ls)(a, i, j)
      //or Swapped_shallow_eq(ls)(a, i, j)

    def Swapped1[T:Iso:Eq](ls: (Lc,Lc))(a: Vec[T]) =
      exists{i: Rep[Int] => exists{j: Rep[Int] => Swapped(ls)(a,i,j)}}

    def permut[T:Iso:Eq] =
      reflexiveTransitiveClosure[Vec[T]](Swapped1, as="Permut")

    def permut_alt[T:Iso:Eq] = inductive[(Lc,Lc),Vec[T]](
      implicitly[Iso[T]].id+"_Permut", { p =>
        add_closure_cases(p)
        add_case[(Lc,Lc),Vec[T]]("swap", { ls => v =>
          forall{i: Rep[Int] => forall{j: Rep[Int] =>
            (0 <= i && i < v.length && 0 <= j && j < v.length &&
             Swapped(ls)(v, i, j)) ==>
            (p(ls)(v))
          }}
        })
      })

    trait Ord[T] {
      def le: (T,T) => Rep[Boolean]
      def id: String = ""
    }
    def infix_cmp[T:Ord](x: T, y: T) =
      implicitly[Ord[T]].le(x,y)
    def by_key[T:Ord] = {
      val id = implicitly[Ord[T]].id
      (if (id.isEmpty) "" else "_"+id)
    }
    def ord[T](_le: (T,T) => Rep[Boolean], _id: String = "") = new Ord[T]{
      override def le = _le
      override def id = _id
    }

    def sortedSlice[T:Iso:Ord](v: Vec[T], start: Rep[Int], end: Rep[Int]) = forall{i: Rep[Int] =>
      (start <= i && i < end) ==> (v(i) cmp v(i+1))
    }
    def Sorted[T:Iso:Ord](v: Vec[T]) = sortedSlice[T](v, 0, v.length-1)
    case class VecRange[T:Iso:Ord](v: Vec[T], start: Rep[Int], end: Rep[Int]) {
      def sorted = sortedSlice[T](v, start, end)
      def forall(p: T => Rep[Boolean]) = dsl.forall{i: Rep[Int] =>
        (start <= i && i < end) ==> p(v(i))
      }
    }
    def infix_slice[T:Iso:Ord](v: Vec[T], i: Rep[Int], j: Rep[Int]) = VecRange(v, i, j)

    def insort[T:Iso:Eq:Ord] = {
      val Permut = permut[T]
      val inswap = toplevel("inswap_"+key[T], { (v: Vec[T], i: Rep[Int], j: Rep[Int]) =>
        val (p, n) = (v.a, v.n)
        requires(0 <= i && i < n && 0 <= j && j < n)
        ensures{result: Rep[Unit] => Swapped(("Old", "Post"))(v, i, j)}
        ensures{result: Rep[Unit] => Permut(("Old","Post"))(v)}
        v.reflectMutable
        val tmp = p(i)
        p(i) = p(j)
        p(j) = tmp
        unit(())
      })

      toplevel("insort"+by_key[T], { (a: Vec[T]) =>
        a.reflectMutable
        val p = a.a
        val n = a.length
        // TODO: add blame on cmp
        // TODO: quantify over any elements, not indices --
        //       first requires implementing forall construct on Iso vs Rep.
        _assert{(0 until n).forall{i => (0 until n).forall{j => (0 until n).forall{k =>
          ((a(i) cmp a(j)) && (a(j) cmp a(k))) ==> (a(i) cmp a(k))
        }}}}
        ensures{result: Rep[Unit] => Sorted(a) && Permut(("Old","Post"))(a)}
        for (i <- 0 until (n-1)) {
          loop_assigns(list_new(i::p.within(0 until n))) // TODO: infer?
          loop_invariant(a.slice(0,i).sorted)
          loop_invariant((i > 0) ==> a.slice(i,n).forall(a(i-1) cmp _))
          loop_invariant(Permut(("Pre","Here"))(a))
          loop_invariant(a.separated)
          loop_invariant(a.check)
          var jmin = i
          for (j <- (i+1) until n) {
            loop_invariant(a.slice(i,j).forall(a(jmin) cmp _))
            loop_invariant(i <= jmin && jmin < j)
            if (a(j) cmp a(jmin)) jmin = j
            else _assert(a(jmin) cmp a(j))
          }
          //_assert(a.slice(i,n).forall(a(jmin) cmp _))
          _assert(a(jmin) cmp a(i+1))
          //_assert(a.slice(0,i).sorted)
          inswap(a,i,jmin)
          _assert(a.slice(0,i-1).sorted)
          _assert(a.slice(0,i).sorted)
          _assert(a(i) cmp a(i+1))
          _assert(a.slice(0,i+1).sorted)
          _assert(a.slice(i+1,n).forall(a(i) cmp _))
        }
      })
    }

    def pointWise[T](p: (T,T) => Rep[Boolean]) = { (a: (T,T), b: (T,T)) =>
      p(a._1, b._1) && p(a._2, b._2)
    }
    implicit def eq_pair[T:Eq:Iso] = equality[(T,T)](pointWise(_ deep_equal _))
  }

  test("1") {
    trait Srt1 extends Sorting {
      implicit def o = ord[Rep[Int]](_ <= _)
      val s = insort[Rep[Int]]
    }
    check("1", (new Srt1 with Impl).code)
  }

  test("2") {
    trait Srt2 extends Sorting {
      implicit def o = ord[Rep[Int]](_ >= _)
      val s = insort[Rep[Int]]
    }
    check("2", (new Srt2 with Impl).code)
  }

  test("3") {
    trait Srt3 extends Sorting {
      implicit def o = ord[(Rep[Int],Rep[Int])]({ (a: (Rep[Int],Rep[Int]), b: (Rep[Int],Rep[Int])) =>
        (a._1<=b._1)
      }, "pairs")
      val s = insort[(Rep[Int],Rep[Int])]
    }
    check("3", (new Srt3 with Impl).code)
  }

  test("4") {
    trait Srt4 extends Sorting {
      implicit def o = ord[(Rep[Int],Rep[Int])]({ (a: (Rep[Int],Rep[Int]), b: (Rep[Int],Rep[Int])) =>
        (a._1 < b._1) || ((a._1==b._1) && (a._2 <= b._2))
      }, "pairs")
      val s = insort[(Rep[Int],Rep[Int])]
    }
    check("4", (new Srt4 with Impl).code)
  }

  test("5") {
    trait Srt5 extends Sorting {
      implicit def o = ord[Vec[Rep[Int]]]({ (a: Vec[Rep[Int]], b: Vec[Rep[Int]]) =>
        (a.length <= b.length)
      }, "vecs")
      val s = insort[Vec[Rep[Int]]]
    }
    check("5", (new Srt5 with Impl).code)
  }

  test("6") {
    trait Srt6 extends Sorting {
      implicit def o = ord[Vec[Vec[Rep[Int]]]]({ (a: Vec[Vec[Rep[Int]]], b: Vec[Vec[Rep[Int]]]) =>
        (a.length <= b.length)
      }, "vv")
      val s = insort[Vec[Vec[Rep[Int]]]]
    }
    // TODO: inswap postcondition does not verify
    // due to too much nesting confusing Frama-C in separation guarantees (?)
    //check("6", (new Srt6 with Impl).code)
  }

  test("7") {
    trait Srt7 extends Sorting {
      type TL = Vec[Vec[Vec[Vec[Vec[Rep[Int]]]]]]
      implicit def o = ord[TL]({ (a: TL, b: TL) =>
        (a.length <= b.length)
      }, "vvvvv")
      val s = insort[TL]
    }
    // TODO: ditto
    //check("7", (new Srt7 with Impl).code)
  }
}
