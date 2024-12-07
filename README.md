# JPABook

김영한님의 jpa 활용편 1 수강 내역

기억해 두어야 할 사항
1. 다대다 관계는 지양되어야 하고, 자동 생성된 테이블을 이용하는 것보단 직접 중간 테이블을 이용한다. 
자동 생성된 테이블은 컬럼을 추가할 수 없다.
2. 한 번 엔티티에 들어갔었던 자원 (ex. id가 추가된 데이터)는 영속성 컨텍스트에 포함되지 않는다. 
따라서 업데이트가 필요한 데이터를 한 번 조회하여 영속성 컨텍스트에 의한 자원 관리 상태로 두고 데이터 업데이트를 한다.
여기에서 업데이트는 업데이트 쿼리를 날리는 것이 아니라 객체에 담긴 데이터를 변경하는 것이다.
이렇게 하면 JPA에서 자동으로 변경 감지를 하여 업데이트 쿼리를 날린다.
3. 도메인 주도 개발을 하자. 그러면 객체가 어디에서 생성되는지, 변경되는지 관리가 쉬워진다. 엔티티에 객체 생성 로직 또한 위임한다.

별개 내용
1. 만약 데이터 구조층이 io > dto > entity 순으로 흐를 경우, dto엔 엔티티와 타입을 맞춘다.
2. 만약 빌더 패턴으로 entity를 상속받으려면 @SuperBuilder 어노테이션을 부모와 자식에게 맞추어 주어야 한다.

12.03 김영한님의 jpa 활용편 2 수강 시작
1. 지연 로딩으로 설정한 필드에 있어서 패치 조인을 이용하자
2. 컬렉션을 불러올 시 배치조인이란 어노테이션을 이용할 것