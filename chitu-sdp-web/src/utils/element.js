/*
 * @description: 按需引入element组件
 * @Author: lijianguo19
 * @Date: 2022-06-10 16:03:05
 * @FilePath: \src\utils\element.js
 */
import Vue from 'vue';
import { Dropdown, DropdownMenu,DropdownItem,Icon,Tooltip,Input ,Badge,Select,Option,Scrollbar} from 'element-ui';
Vue.use(DropdownMenu);
Vue.use(DropdownItem);
Vue.use(Icon);
Vue.use(Tooltip);
Vue.use(Input);
Vue.use(Dropdown);
Vue.use(Badge);
Vue.use(Select);
Vue.use(Option);
Vue.use(Scrollbar);