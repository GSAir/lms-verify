//#include <stdio.h>

#define N 100

/*@ requires na > 0 && nb > 0 && na == nb;
    requires \valid(va+(0..na-1));
    requires \valid(vb+(0..nb-1));
    assigns \nothing;
 */
int vv_mult(int* va, int na, int* vb, int nb) {
  int r = 0;
  /*@
    loop invariant 0<=i<=na;
    loop assigns i, r;
  */
  for (int i = 0; i<na; i++) {
    r += va[i]*vb[i];
  }
  return r;
}

/*@ requires ra > 0 && ca > 0;
    requires ra < N && ca < N;
    requires 0 <= r < ra;
    requires 0 <= c < ca;
    ensures 0 <= \result < ra*ca;
    assigns \nothing;
*/
int index(int r, int c, int ra, int ca) {
  return r*ca+c;
}

/*@ requires ra > 0 && ca > 0 && nb > 0 && nc > 0;
    requires ca == nb;
    requires ra == nc;
    requires \valid(vb+(0..nb-1));
    requires \valid(vc+(0..nc-1));
    requires ra<N && ca<N;
    requires \valid(ma+(0..ra*ca-1));
    requires \separated(ma, vc);
    requires \separated(vb, vc);
    assigns vc[0..nc-1];
*/
void mv_mult(int* ma, int ra, int ca,
             int* vb, int nb,
             int* vc, int nc) {
  /*@
    loop invariant 0 <= r <= ra;
    loop assigns r, vc[0..nc-1];
  */
  for (int r = 0; r < ra; r++) {
    vc[r] = 0;
    /*@
      loop invariant 0 <= c <= ca;
      loop assigns c, vc[0..nc-1];
    */
    for (int c = 0; c < ca; c++) {
      vc[r] += ma[index(r, c, ra, ca)]*vb[c];
    }
  }
  return;
}

/*@ requires ra > 0 && ca > 0;
    requires rb > 0 && cb > 0;
    requires rc > 0 && cc > 0;
    requires ca == rb;
    requires ra == rc;
    requires cc == cb;
    requires ra<N && ca<N;
    requires \valid(ma+(0..ra*ca-1));
    requires rb<N && cb<N;
    requires \valid(mb+(0..rb*cb-1));
    requires rc<N && cc<N;
    requires \valid(mc+(0..rc*cc-1));
    requires \separated(ma, mc);
    requires \separated(mb, mc);
    assigns mc[0..rc*cc-1];
*/
void mm_mult(int* ma, int ra, int ca,
             int* mb, int rb, int cb,
             int* mc, int rc, int cc) {
  /*@
    loop invariant 0 <= r <= ra;
    loop assigns r, mc[0..rc*cc-1];
  */
  for (int r = 0; r < ra; r++) {
    /*@
      loop invariant 0 <= c <= cb;
      loop assigns c, mc[0..rc*cc-1];
    */
    for (int c = 0; c < cb; c++) {
      mc[index(r, c, rc, cc)] = 0;
      /*@
        loop invariant 0 <= i <= ca;
        loop assigns i, mc[0..rc*cc-1];
      */
      for (int i = 0; i < ca; i++) {
        mc[index(r, c, rc, cc)] += ma[index(r, i, ra, ca)] * mb[index(i, c, rb, cb)];
      }
    }
  }
  return;
}

int main(void) {
  int ma[] = {1, 2, 3, 4, 5, 6};
  int ra = 2; int ca = 3;

  int mb[] = {1, 2, 3, 4, 5, 6};
  int rb = 3; int cb = 2;

  int mc[] = {0, 0, 0, 0};
  int rc = 2; int cc = 2;

  mm_mult(ma, ra, ca, mb, rb, cb, mc, rc, cc);

  /*
  for (int i = 0; i < rc; i++) {
    for (int j = 0; j < cc; j++) {
      printf("%d ", mc[i*cc+j]);
    }
    printf("\n");
  }
  */

  return 0;
}
