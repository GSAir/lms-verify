#include <limits.h>
//@ predicate eq___Int_Int__(int  x0, int  x1, int  x2, int  x3) = ((x0==x2) && (x1==x3));
/*@
assigns \nothing;
ensures \result <==> eq___Int_Int__(x0, x1, x2, x3);
*/
int eq___Int_Int__(int  x0, int  x1, int  x2, int  x3) {
  int x5 = x0 == x2;
  int x7;
  if (x5) {
    int x6 = x1 == x3;
    x7 = x6;
  } else {
    x7 = 0/*false*/;
  }
  return x7;
}
/*@
inductive __Int_Int___Permut{L1,L2}(int  * x8, int  * x9, integer  x10) {
  case __Int_Int___Permut_refl{L}:
  \forall int  * x11, int  * x12, integer  x13; __Int_Int___Permut{L,L}(x11,x12,x13);
  case __Int_Int___Permut_sym{L1,L2}:
  \forall int  * x17, int  * x18, integer  x19; (__Int_Int___Permut{L1,L2}(x17,x18,x19) ==> __Int_Int___Permut{L2,L1}(x17,x18,x19));
  case __Int_Int___Permut_trans{L1,L2,L3}:
  \forall int  * x25, int  * x26, integer  x27; ((__Int_Int___Permut{L1,L2}(x25,x26,x27) && __Int_Int___Permut{L2,L3}(x25,x26,x27)) ==> __Int_Int___Permut{L1,L3}(x25,x26,x27));
  case __Int_Int___Permut_swap{L1,L2}:
  \forall int  * x36, int  * x37, integer  x38; (\forall integer  x40; (\forall integer  x41; ((((((((0<=x40) && (x40<x38)) && (0<=x41)) && (x41<x38)) && eq___Int_Int__(\at(x36[x40],L1),\at(x37[x40],L1),\at(x36[x41],L2),\at(x37[x41],L2))) && eq___Int_Int__(\at(x36[x41],L1),\at(x37[x41],L1),\at(x36[x40],L2),\at(x37[x40],L2))) && (\forall integer  x74; (((((0<=x74) && (x74<x38)) && (x74!=x40)) && (x74!=x41)) ==> eq___Int_Int__(\at(x36[x74],L1),\at(x37[x74],L1),\at(x36[x74],L2),\at(x37[x74],L2))))) ==> __Int_Int___Permut{L1,L2}(x36,x37,x38))));
}
*/
/*@
requires (((((((x109>0) && (0<=x107)) && (x107<x109)) && (0<=x108)) && (x108<x109)) && (\valid(x105+(0..x109-1)) && \valid(x106+(0..x109-1)))) && (\forall int  x139; (\forall int  x140; (((((0<=x139) && (x139<x109)) && (0<=x140)) && (x140<x109)) ==> \separated(x105+x139,x106+x140)))));
ensures ((((eq___Int_Int__(x105[x107],x106[x107],\old(x105[x108]),\old(x106[x108])) && eq___Int_Int__(x105[x108],x106[x108],\old(x105[x107]),\old(x106[x107]))) && (\forall int  x175; ((((0<=x175) && (x175<x109)) && ((x175!=x107) && (x175!=x108))) ==> eq___Int_Int__(x105[x175],x106[x175],\old(x105[x175]),\old(x106[x175]))))) && __Int_Int___Permut{Old,Post}(x105,x106,x109)) && (\forall int  x199; (\forall int  x200; (((((0<=x199) && (x199<x109)) && (0<=x200)) && (x200<x109)) ==> \separated(x105+x199,x106+x200)))));
assigns x105[(0..x109-1)], x106[(0..x109-1)];
*/
void inswap___Int_Int__(int  * x105, int  * x106, int  x107, int  x108, int  x109) {
  int x112 = x105[x107];
  int x113 = x106[x107];
  int x114 = x105[x108];
  int x115 = x106[x108];
  x105[x107] = x114;
  x106[x107] = x115;
  x105[x108] = x112;
  x106[x108] = x113;
}
/*@
requires (((x221>0) && (\valid(x219+(0..x221-1)) && \valid(x220+(0..x221-1)))) && (\forall int  x473; (\forall int  x474; (((((0<=x473) && (x473<x221)) && (0<=x474)) && (x474<x221)) ==> \separated(x219+x473,x220+x474)))));
ensures (((\forall int  x493; (((0<=x493) && (x493<(x221-1))) ==> (x219[x493]<=x219[(x493+1)]))) && __Int_Int___Permut{Old,Post}(x219,x220,x221)) && (\forall int  x510; (\forall int  x511; (((((0<=x510) && (x510<x221)) && (0<=x511)) && (x511<x221)) ==> \separated(x219+x510,x220+x511)))));
assigns x219[(0..x221-1)], x220[(0..x221-1)];
*/
void insort_pairs(int  * x219, int  * x220, int  x221) {
  int x224 = x221;
  /*@
  loop invariant ((((((0<=x224) && (x224<=x221)) && ((x224<(x221-1)) ==> (\forall int  x392; (((x224<=x392) && (x392<(x221-1))) ==> (x219[x392]<=x219[(x392+1)]))))) && (\forall int  x411; ((((0<=x411) && (x411<x224)) && (x224<=(x221-1))) ==> (x219[x411]<=x219[x224])))) && __Int_Int___Permut{Pre,Here}(x219,x220,x221)) && (\forall int  x436; (\forall int  x437; (((((0<=x436) && (x436<x221)) && (0<=x437)) && (x437<x221)) ==> \separated(x219+x436,x220+x437)))));
  loop assigns x224, x219[(0..x221-1)], x220[(0..x221-1)];
  loop variant x224;
  */
  for (;;) {
    int x225 = x224;
    int x226 = x225 > 1;
    if (!x226) break;
    int x228 = 0;
    int x229 = x224;
    /*@
    loop invariant ((((((((((0<=x224) && (x224<=x221)) && (0<=x231)) && (x231<=x224)) && (0<=x228)) && (x228<=(x224-1))) && ((x224-1)<x221)) && (\forall int  x280; (((0<=x280) && (x280<x231)) ==> (x219[x280]<=x219[x228])))) && __Int_Int___Permut{Pre,Here}(x219,x220,x221)) && (\forall int  x299; (\forall int  x300; (((((0<=x299) && (x299<x221)) && (0<=x300)) && (x300<x221)) ==> \separated(x219+x299,x220+x300)))));
    loop assigns x231, x228;
    loop variant (x224-x231);
    */
    for(int x231=0; x231 < x229; x231++) {
      int x232 = x228;
      int x233 = x219[x232];
      int x234 = x220[x232];
      int x235 = x219[x231];
      int x236 = x220[x231];
      int x237 = x233 <= x235;
      if (x237) {
        x228 = x231;
      } else {
        //@assert (x219[x231]<=x219[x228]);
      }
    }
    int x327 = x228;
    int x326 = x229 - 1;
    inswap___Int_Int__(x219,x220,x326,x327,x221);
    //@assert (\forall int  x329; ((((x224-1)<x329) && (x329<(x221-1))) ==> (x219[x329]<=x219[(x329+1)])));
    //@assert ((x224<=(x221-1)) ==> (x219[(x224-1)]<=x219[x224]));
    //@assert (\forall int  x362; (((0<=x362) && (x362<x224)) ==> (x219[x362]<=x219[(x224-1)])));
    x224 = x326;
  }
}
