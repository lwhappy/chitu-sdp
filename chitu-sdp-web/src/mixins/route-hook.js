export default {
    methods: {
        SET_SAVE_HOOK_FLAG (val = true) {
            this._save_hook_flag_ = val
        }
    },
    beforeRouteLeave (to, from, next) {
        // console.log(to, from, this)
        // debugger
        if (!this._save_hook_flag_ && to.meta.tag === from.meta.tag && !to.query.changeEnv) {
            this.$confirm('您确定要取消保存当前数据？', '提示', { showClose: false })
                .then(() => next())
                .catch(() => next(false))
        } else {
            this.SET_SAVE_HOOK_FLAG(false)
            next()
        }
    }
}