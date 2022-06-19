package ch3

class TestUser1(name: String, id: Int) {
    private val name: String = name
    private val id: Int = id

    // 부생성자는 항상 주생성자를 호출해야할 책임을 갖는다.
    constructor(id: Int) : this("", id)
}

class TestUser2(namee: String, idd: Int) {
    private val name: String
    private val id: Int

    // init은 주 생성자에 병합된다!
    init {
        this.name = namee + ""
        this.id = idd + 1
    }

    constructor(name: String) : this(name, 0)
}

// 초기화 블럭(init{})과 프로퍼티 할당 순차적으로 이뤄진다.
class TestUser3(namee: String, idd: Int) {

    // 1. print
    init {
        println("test")
    }

    // 2.id를 idd 값으로 초기화
    private val name: String
    private val id: Int = idd

    // 3. name을 namee 값으로 초기화
    init {
        this.name = namee
    }
}

// 주 생성자에 프로퍼티를 직접 할당할 수 있다.
class TestUser4(private val namee: String, private val id: Int) {

    // 1. 가장 먼저 namee, id 프로퍼티가 할당된다.
    // 2. 그 다음 init 블럭이 순차적으로 실행된다. (주 생성자에 병합됨)

    init {
        println("init1")
    }

    constructor(name: String): this("$name-", 0)

    init {
        println("init2")
    }
}
