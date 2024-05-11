package com.geeks.hobbys.ui.model

class User {
    var uid:String? = null
    var name:String?= null
    var email:String? =null
    var profileImg:String? = null
    constructor(){}
    constructor(
        uid:String?,
        name:String,
        email:String?,
        profileImg:String?
    )
    {
        this.uid = uid
        this.name = name
        this.email = email
        this.profileImg = profileImg
    }
}