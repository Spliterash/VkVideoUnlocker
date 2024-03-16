package ru.spliterash.vkVideoUnlocker.group

import io.micronaut.context.annotation.Value
import jakarta.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.spliterash.vkVideoUnlocker.group.dto.GroupInfo
import ru.spliterash.vkVideoUnlocker.group.dto.GroupStatus
import ru.spliterash.vkVideoUnlocker.group.dto.MemberStatus
import ru.spliterash.vkVideoUnlocker.video.exceptions.VideoGroupPrivateException
import ru.spliterash.vkVideoUnlocker.video.exceptions.VideoGroupRequestSendException
import ru.spliterash.vkVideoUnlocker.vk.actor.types.WorkUser
import ru.spliterash.vkVideoUnlocker.vk.api.VkApi

@Singleton
class WorkUserGroupService(
    @WorkUser private val user: VkApi,
    @Value("\${vk-unlocker.private-groups:false}") private val privateGroupWork: Boolean

) {
    private val groups = hashMapOf<Int, GroupInfo>()
    private val lock = Mutex()

    /**
     * Если группа открытая ничего не делать
     * Если группа закрытая - отправить заявку если её нет, выкинуть эксепшн
     * Если группа частная - выкинуть эксепшн
     *
     * Результаты кешируются
     *
     * @return Статус группы
     */
    @Throws(VideoGroupPrivateException::class, VideoGroupRequestSendException::class)
    suspend fun joinGroup(groupId: Int): GroupStatus {
        val groupInfo = lock.withLock { groups[groupId] }
        if (groupInfo != null) {
            val groupIsOpen = groupInfo.groupStatus == GroupStatus.PUBLIC
            val imMember = groupInfo.memberStatus == MemberStatus.MEMBER
            if (groupIsOpen || imMember)
                return groupInfo.groupStatus

            if (groupInfo.groupStatus == GroupStatus.PRIVATE)
                throw VideoGroupPrivateException()

        }

        val actualInfo = user.groups.status(groupId)

        try {
            when (actualInfo.groupStatus) {
                GroupStatus.CLOSE -> {
                    if (!privateGroupWork) return GroupStatus.CLOSE
                    when (actualInfo.memberStatus) {
                        MemberStatus.NO -> {
                            user.groups.join(groupId)
                            actualInfo.memberStatus = MemberStatus.REQUEST_SEND
                            throw VideoGroupRequestSendException()
                        }

                        MemberStatus.REQUEST_SEND -> throw VideoGroupRequestSendException()
                        else -> Unit
                    }
                }

                GroupStatus.PRIVATE -> {
                    if (actualInfo.memberStatus != MemberStatus.MEMBER)
                        throw VideoGroupPrivateException()
                }

                GroupStatus.PUBLIC -> {
                    if (actualInfo.memberStatus == MemberStatus.NO) {
                        user.groups.join(groupId)
                        actualInfo.memberStatus = MemberStatus.MEMBER
                    }
                }
            }
            lock.withLock { groups[groupId] = actualInfo }
            return actualInfo.groupStatus
        } finally {
            lock.withLock { groups[groupId] = actualInfo }
        }
    }
}