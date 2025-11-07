package ai.p2ach.p2achandroidvision.viewmodels

import ai.p2ach.p2achandroidlibrary.base.viewmodel.BaseViewModel
import ai.p2ach.p2achandroidvision.repos.MDMEntity
import ai.p2ach.p2achandroidvision.repos.MDMRepo


class MdmViewModel(private val mdmRepo: MDMRepo) : BaseViewModel<MDMEntity, MDMRepo>(mdmRepo) {


}