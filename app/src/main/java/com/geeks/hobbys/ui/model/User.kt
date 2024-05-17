package com.geeks.hobbys.ui.model

class User {
    var uid:String? = null
    var name:String?= null
    var hobby:String?= null
    var email:String? =null
    var profileImg:String? = null
    constructor(){}
    constructor(
        uid:String?,
        name:String,
        hobby:String,
        email:String?,
        profileImg:String?
    )
    {
        this.uid = uid
        this.name = name
        this.hobby = hobby
        this.email = email
        this.profileImg = profileImg
    }
}