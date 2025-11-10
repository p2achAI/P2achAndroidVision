package ai.p2ach.p2achandroidvision.viewmodels

import ai.p2ach.p2achandroidlibrary.base.viewmodel.BaseViewModel
import ai.p2ach.p2achandroidvision.repos.mdm.MDMEntity
import ai.p2ach.p2achandroidvision.repos.mdm.MDMRepo


class MdmViewModel(private val mdmRepo: MDMRepo) : BaseViewModel<MDMEntity, MDMRepo>(mdmRepo) {


}