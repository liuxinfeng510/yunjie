-- =====================================================
-- V22: 生产企业种子数据 - 预置国内药房常见药品生产企业
-- =====================================================

INSERT INTO drug_manufacturer (tenant_id, name, short_name, pinyin, pinyin_short, address, status)
SELECT 0, t.name, t.short_name, t.pinyin, t.pinyin_short, t.address, 'active'
FROM (
-- ===== 大型国内制药集团 =====
SELECT '石药集团欧意药业有限公司' as name, '石药欧意' as short_name, 'shiyaojituanouyiyaoye' as pinyin, 'syjtoyyy' as pinyin_short, '河北省石家庄市' as address UNION ALL
SELECT '齐鲁制药有限公司', '齐鲁制药', 'qiluzhiyao', 'qlzy', '山东省济南市' UNION ALL
SELECT '扬子江药业集团有限公司', '扬子江', 'yangzijiangyaoye', 'yzjyy', '江苏省泰州市' UNION ALL
SELECT '江苏恒瑞医药股份有限公司', '恒瑞医药', 'hengruiyiyao', 'hryy', '江苏省连云港市' UNION ALL
SELECT '正大天晴药业集团股份有限公司', '正大天晴', 'zhengdatianqing', 'zdtq', '江苏省连云港市' UNION ALL
SELECT '石药集团中诺药业(石家庄)有限公司', '石药中诺', 'shiyaozhongnuo', 'syzn', '河北省石家庄市' UNION ALL
SELECT '华润双鹤药业股份有限公司', '华润双鹤', 'huarunshuanghe', 'hrsh', '北京市' UNION ALL
SELECT '中国医药集团有限公司', '国药集团', 'guoyaojituan', 'gyjt', '北京市' UNION ALL
SELECT '上海医药集团股份有限公司', '上药集团', 'shangyaojituan', 'syjt', '上海市' UNION ALL
SELECT '广州医药集团有限公司', '广药集团', 'guangyaojituan', 'gyjt', '广东省广州市' UNION ALL
SELECT '天津药业集团有限公司', '天药集团', 'tianyaojituan', 'tyjt', '天津市' UNION ALL
SELECT '浙江医药股份有限公司', '浙江医药', 'zhejiangyiyao', 'zjyy', '浙江省绍兴市' UNION ALL
SELECT '上海现代制药股份有限公司', '现代制药', 'xiandaizhiyao', 'xdzy', '上海市' UNION ALL
SELECT '东北制药集团股份有限公司', '东北制药', 'dongbeizhiyao', 'dbzy', '辽宁省沈阳市' UNION ALL
SELECT '江苏豪森药业集团有限公司', '豪森药业', 'haosenyaoye', 'hsyy', '江苏省连云港市' UNION ALL
SELECT '深圳信立泰药业股份有限公司', '信立泰', 'xinlitai', 'xlt', '广东省深圳市' UNION ALL
SELECT '丽珠医药集团股份有限公司', '丽珠集团', 'lizhujituan', 'lzjt', '广东省珠海市' UNION ALL
SELECT '海正药业股份有限公司', '海正药业', 'haizhengyaoye', 'hzyy', '浙江省台州市' UNION ALL
SELECT '人福医药集团股份有限公司', '人福医药', 'renfuyiyao', 'rfyy', '湖北省武汉市' UNION ALL
SELECT '科伦药业股份有限公司', '科伦药业', 'kelunyaoye', 'klyy', '四川省成都市' UNION ALL
SELECT '华东医药股份有限公司', '华东医药', 'huadongyiyao', 'hdyy', '浙江省杭州市' UNION ALL
SELECT '鲁南制药集团股份有限公司', '鲁南制药', 'lunanzhiyao', 'lnzy', '山东省临沂市' UNION ALL
SELECT '四川科伦药业股份有限公司', '四川科伦', 'sichuankelun', 'sckl', '四川省成都市' UNION ALL
SELECT '石家庄以岭药业股份有限公司', '以岭药业', 'yilingyaoye', 'ylyy', '河北省石家庄市' UNION ALL
SELECT '瑞阳制药股份有限公司', '瑞阳制药', 'ruiyangzhiyao', 'ryzy', '山东省淄博市' UNION ALL

-- ===== 抗生素/原料药企业 =====
SELECT '华北制药股份有限公司', '华北制药', 'huabeizhiyao', 'hbzy', '河北省石家庄市' UNION ALL
SELECT '哈药集团制药总厂', '哈药集团', 'hayaojituan', 'hyjt', '黑龙江省哈尔滨市' UNION ALL
SELECT '鲁抗医药股份有限公司', '鲁抗医药', 'lukangyiyao', 'lkyy', '山东省济宁市' UNION ALL
SELECT '联邦制药国际控股有限公司', '联邦制药', 'lianbangzhiyao', 'lbzy', '广东省珠海市' UNION ALL
SELECT '山东罗欣药业集团股份有限公司', '罗欣药业', 'luoxinyaoye', 'lxyy', '山东省临沂市' UNION ALL
SELECT '广州白云山制药股份有限公司', '白云山制药', 'baiyunshanzhiyao', 'byszyy', '广东省广州市' UNION ALL
SELECT '珠海联邦制药股份有限公司', '珠海联邦', 'zhuhailinabang', 'zhlb', '广东省珠海市' UNION ALL
SELECT '山东新华制药股份有限公司', '新华制药', 'xinhuazhiyao', 'xhzy', '山东省淄博市' UNION ALL
SELECT '浙江京新药业股份有限公司', '京新药业', 'jingxinyaoye', 'jxyy', '浙江省金华市' UNION ALL
SELECT '苏州东瑞制药有限公司', '东瑞制药', 'dongruizhiyao', 'drzy', '江苏省苏州市' UNION ALL
SELECT '广东南国药业有限公司', '南国药业', 'nanguoyaoye', 'ngyy', '广东省佛山市' UNION ALL
SELECT '湖南科伦制药有限公司', '湖南科伦', 'hunankelun', 'hnkl', '湖南省岳阳市' UNION ALL
SELECT '江西济民可信集团有限公司', '济民可信', 'jiminkexin', 'jmkx', '江西省南昌市' UNION ALL
SELECT '山西振东制药股份有限公司', '振东制药', 'zhendongzhiyao', 'zdzy', '山西省长治市' UNION ALL
SELECT '浙江海正药业股份有限公司', '浙江海正', 'zhejianghaizheng', 'zjhz', '浙江省台州市' UNION ALL

-- ===== OTC/家庭常用药企业 =====
SELECT '中美天津史克制药有限公司', '天津史克', 'tianjinshike', 'tjsk', '天津市' UNION ALL
SELECT '上海强生制药有限公司', '上海强生', 'shanghqiangsheng', 'shqs', '上海市' UNION ALL
SELECT '拜耳医药保健有限公司', '拜耳', 'baier', 'be', '北京市' UNION ALL
SELECT '葵花药业集团股份有限公司', '葵花药业', 'kuihuayaoye', 'khyy', '黑龙江省哈尔滨市' UNION ALL
SELECT '修正药业集团股份有限公司', '修正药业', 'xiuzhengyaoye', 'xzyy', '吉林省通化市' UNION ALL
SELECT '吉林敖东药业集团股份有限公司', '敖东药业', 'aodongyaoye', 'adyy', '吉林省延边州' UNION ALL
SELECT '哈药集团三精制药股份有限公司', '三精制药', 'sanjingzhiyao', 'sjzy', '黑龙江省哈尔滨市' UNION ALL
SELECT '江中药业股份有限公司', '江中药业', 'jiangzhongyaoye', 'jzyy', '江西省南昌市' UNION ALL
SELECT '华润三九医药股份有限公司', '华润三九', 'huarunsanjiu', 'hrsj', '广东省深圳市' UNION ALL
SELECT '贵州百灵企业集团制药股份有限公司', '百灵制药', 'bailingzhiyao', 'blzy', '贵州省安顺市' UNION ALL
SELECT '太极集团有限公司', '太极集团', 'taijijituan', 'tjjt', '重庆市' UNION ALL
SELECT '仁和药业股份有限公司', '仁和药业', 'renheyaoye', 'rhyy', '江西省樟树市' UNION ALL
SELECT '西安杨森制药有限公司', '西安杨森', 'xianyangsen', 'xays', '陕西省西安市' UNION ALL
SELECT '东阿阿胶股份有限公司', '东阿阿胶', 'dongeejiao', 'deej', '山东省聊城市' UNION ALL
SELECT '健康元药业集团股份有限公司', '健康元', 'jiankanyuan', 'jky', '广东省深圳市' UNION ALL

-- ===== 中成药企业 =====
SELECT '北京同仁堂股份有限公司', '同仁堂', 'tongrentang', 'trt', '北京市' UNION ALL
SELECT '云南白药集团股份有限公司', '云南白药', 'yunnanbaiyao', 'ynby', '云南省昆明市' UNION ALL
SELECT '漳州片仔癀药业股份有限公司', '片仔癀', 'pianzaihuang', 'pzh', '福建省漳州市' UNION ALL
SELECT '天士力医药集团股份有限公司', '天士力', 'tianshili', 'tsl', '天津市' UNION ALL
SELECT '步长制药股份有限公司', '步长制药', 'buchangzhiyao', 'bczy', '陕西省西安市' UNION ALL
SELECT '济川药业集团有限公司', '济川药业', 'jichuanyaoye', 'jcyy', '江苏省泰州市' UNION ALL
SELECT '广州白云山和记黄埔中药有限公司', '和记黄埔', 'hejihuangpu', 'hjhp', '广东省广州市' UNION ALL
SELECT '天津中新药业集团股份有限公司', '中新药业', 'zhongxinyaoye', 'zxyy', '天津市' UNION ALL
SELECT '兰州佛慈制药股份有限公司', '佛慈制药', 'focizhiyao', 'fczy', '甘肃省兰州市' UNION ALL
SELECT '南京同仁堂药业有限责任公司', '南京同仁堂', 'nanjingtongrentang', 'njtrt', '江苏省南京市' UNION ALL
SELECT '广东一方制药有限公司', '一方制药', 'yifangzhiyao', 'yfzy', '广东省佛山市' UNION ALL
SELECT '北京同仁堂科技发展股份有限公司', '同仁堂科技', 'tongrentangkeji', 'trtkj', '北京市' UNION ALL
SELECT '雷允上药业集团有限公司', '雷允上', 'leiyunshang', 'lys', '江苏省苏州市' UNION ALL
SELECT '广西金嗓子有限责任公司', '金嗓子', 'jinsangzi', 'jsz', '广西壮族自治区柳州市' UNION ALL
SELECT '桂林三金药业股份有限公司', '三金药业', 'sanjinyaoye', 'sjyy', '广西壮族自治区桂林市' UNION ALL
SELECT '四川好医生攀西药业有限责任公司', '好医生', 'haoyisheng', 'hys', '四川省西昌市' UNION ALL
SELECT '贵州益佰制药股份有限公司', '益佰制药', 'yibaizhiyao', 'ybzy', '贵州省贵阳市' UNION ALL
SELECT '昆明制药集团股份有限公司', '昆药集团', 'kunyaojituan', 'kyjt', '云南省昆明市' UNION ALL
SELECT '吉林万通药业集团有限公司', '万通药业', 'wantongyaoye', 'wtyy', '吉林省通化市' UNION ALL
SELECT '山东宏济堂制药集团股份有限公司', '宏济堂', 'hongjitang', 'hjt', '山东省济南市' UNION ALL
SELECT '湖南方盛制药股份有限公司', '方盛制药', 'fangshengzhiyao', 'fszy', '湖南省长沙市' UNION ALL
SELECT '陕西摩美得制药有限公司', '摩美得', 'momeide', 'mmd', '陕西省咸阳市' UNION ALL
SELECT '河南羚锐制药股份有限公司', '羚锐制药', 'lingxiuzhiyao', 'lxzy', '河南省信阳市' UNION ALL
SELECT '河南宛西制药股份有限公司', '宛西制药', 'wanxizhiyao', 'wxzy', '河南省南阳市' UNION ALL
SELECT '山东沃华医药科技股份有限公司', '沃华医药', 'wohuayiyao', 'whyy', '山东省潍坊市' UNION ALL
SELECT '四川光大制药有限公司', '光大制药', 'guangdazhiyao', 'gdzy', '四川省成都市' UNION ALL
SELECT '湖北午时药业股份有限公司', '午时药业', 'wushiyaoye', 'wsyy', '湖北省孝感市' UNION ALL
SELECT '马应龙药业集团股份有限公司', '马应龙', 'mayinglong', 'myl', '湖北省武汉市' UNION ALL

-- ===== 中药饮片企业 =====
SELECT '康美药业股份有限公司', '康美药业', 'kangmeiyaoye', 'kmyy', '广东省普宁市' UNION ALL
SELECT '中国中药控股有限公司', '中国中药', 'zhongguozhongyao', 'zgzy', '北京市' UNION ALL
SELECT '安徽亳州沪谯饮片有限公司', '沪谯饮片', 'huqiaoyinpian', 'hqyp', '安徽省亳州市' UNION ALL
SELECT '四川新荷花中药饮片股份有限公司', '新荷花', 'xinhehua', 'xhh', '四川省成都市' UNION ALL
SELECT '安徽协和成药业饮片有限公司', '协和成', 'xiehecheng', 'xhc', '安徽省亳州市' UNION ALL
SELECT '北京同仁堂(亳州)饮片有限责任公司', '同仁堂饮片', 'tongrentangyinpian', 'trtyp', '安徽省亳州市' UNION ALL
SELECT '安徽九洲方圆制药有限公司', '九洲方圆', 'jiuzhoufangyuan', 'jzfy', '安徽省亳州市' UNION ALL
SELECT '河北百草康神药业有限公司', '百草康神', 'baicaokangshen', 'bcks', '河北省安国市' UNION ALL
SELECT '河北祁新中药颗粒饮片有限公司', '祁新中药', 'qixinzhongyao', 'qxzy', '河北省安国市' UNION ALL
SELECT '四川省中药饮片有限责任公司', '四川饮片', 'sichuanyinpian', 'scyp', '四川省成都市' UNION ALL
SELECT '甘肃陇神戎发药业股份有限公司', 'LSRF', 'longshenrongfa', 'lsrf', '甘肃省兰州市' UNION ALL
SELECT '广东康美药业有限公司', '广东康美', 'guangdongkangmei', 'gdkm', '广东省普宁市' UNION ALL
SELECT '山东一方制药有限公司', '山东一方', 'shandongyifang', 'sdyf', '山东省济南市' UNION ALL
SELECT '浙江中医药大学中药饮片有限公司', '浙中大饮片', 'zhezhongdayinpian', 'zzdyp', '浙江省杭州市' UNION ALL
SELECT '江苏江阴天江药业有限公司', '天江药业', 'tianjiangyaoye', 'tjyy', '江苏省江阴市' UNION ALL

-- ===== 外资/合资药企 =====
SELECT '辉瑞制药有限公司', '辉瑞', 'huirui', 'hr', '上海市' UNION ALL
SELECT '阿斯利康制药有限公司', '阿斯利康', 'asilikang', 'aslk', '江苏省无锡市' UNION ALL
SELECT '诺华制药有限公司', '诺华', 'nuohua', 'nh', '北京市' UNION ALL
SELECT '赛诺菲(中国)投资有限公司', '赛诺菲', 'sainuofei', 'snf', '上海市' UNION ALL
SELECT '默沙东(中国)有限公司', '默沙东', 'moshadong', 'msd', '上海市' UNION ALL
SELECT '罗氏制药有限公司', '罗氏', 'luoshi', 'ls', '上海市' UNION ALL
SELECT '葛兰素史克(中国)投资有限公司', '葛兰素史克', 'gelansushike', 'glssk', '上海市' UNION ALL
SELECT '礼来(中国)制药有限公司', '礼来', 'lilai', 'll', '上海市' UNION ALL
SELECT '勃林格殷格翰(中国)投资有限公司', '勃林格殷格翰', 'bolingeyingehan', 'blygeh', '上海市' UNION ALL
SELECT '诺和诺德(中国)制药有限公司', '诺和诺德', 'nuohenuode', 'nhnd', '天津市' UNION ALL
SELECT '拜耳医药(中国)有限公司', '拜耳医药', 'baieryiyao', 'beyy', '北京市' UNION ALL
SELECT '强生(中国)医疗器材有限公司', '强生', 'qiangsheng', 'qs', '上海市' UNION ALL
SELECT '日本参天制药株式会社', '参天制药', 'cantianzhiyao', 'ctzy', '日本大阪' UNION ALL
SELECT '日本大冢制药株式会社', '大冢制药', 'dazhongzhiyao', 'dzzy', '日本东京' UNION ALL
SELECT '费森尤斯卡比(中国)投资有限公司', '费森尤斯', 'feisenyousi', 'fsys', '北京市' UNION ALL
SELECT '美纳里尼(中国)有限公司', '美纳里尼', 'meinalini', 'mnln', '北京市' UNION ALL
SELECT '施维雅(天津)制药有限公司', '施维雅', 'shiweiya', 'swy', '天津市' UNION ALL
SELECT '卫材(中国)药业有限公司', '卫材', 'weicai', 'wc', '上海市' UNION ALL

-- ===== 生物制药 =====
SELECT '智飞生物制品股份有限公司', '智飞生物', 'zhifeishengwu', 'zfsw', '重庆市' UNION ALL
SELECT '华兰生物工程股份有限公司', '华兰生物', 'hualanshengwu', 'hlsw', '河南省新乡市' UNION ALL
SELECT '长春生物制品研究所有限责任公司', '长春生物', 'changchunshengwu', 'ccsw', '吉林省长春市' UNION ALL
SELECT '沃森生物技术股份有限公司', '沃森生物', 'wosenshengwu', 'wssw', '云南省昆明市' UNION ALL
SELECT '北京天坛生物制品股份有限公司', '天坛生物', 'tiantanshengwu', 'ttsw', '北京市' UNION ALL
SELECT '上海莱士血液制品股份有限公司', '上海莱士', 'shanghlaishi', 'shls', '上海市' UNION ALL
SELECT '成都蓉生药业有限责任公司', '蓉生药业', 'rongshengyaoye', 'rsyy', '四川省成都市' UNION ALL
SELECT '甘李药业股份有限公司', '甘李药业', 'ganliyaoye', 'glyc', '北京市' UNION ALL
SELECT '通化东宝药业股份有限公司', '通化东宝', 'tonghuadongbao', 'thdb', '吉林省通化市' UNION ALL
SELECT '珠海亿胜生物制药有限公司', '亿胜生物', 'yishengshengwu', 'yssw', '广东省珠海市' UNION ALL

-- ===== 皮肤科/外用药 =====
SELECT '滇虹药业集团股份有限公司', '滇虹药业', 'dianhongyaoye', 'dhyy', '云南省昆明市' UNION ALL
SELECT '中美天津史克制药有限公司大众健康部', '天津史克OTC', 'tianjinshikeOTC', 'tjskotc', '天津市' UNION ALL
SELECT '重庆华邦制药有限公司', '华邦制药', 'huabangzhiyao', 'hbzy', '重庆市' UNION ALL
SELECT '四川明欣药业有限公司', '明欣药业', 'mingxinyaoye', 'mxyy', '四川省成都市' UNION ALL
SELECT '湖北人福成田药业有限公司', '人福成田', 'renfuchengtian', 'rfct', '湖北省宜昌市' UNION ALL
SELECT '云南滇虹药业有限公司', '滇虹', 'dianhong', 'dh', '云南省昆明市' UNION ALL
SELECT '海南全星制药有限公司', '全星制药', 'quanxingzhiyao', 'qxzy', '海南省海口市' UNION ALL
SELECT '四川美大康药业股份有限公司', '美大康', 'meidakang', 'mdk', '四川省成都市' UNION ALL

-- ===== 眼科/专科 =====
SELECT '沈阳兴齐眼药股份有限公司', '兴齐眼药', 'xingqiyanyao', 'xqyy', '辽宁省沈阳市' UNION ALL
SELECT '爱尔康(中国)眼科产品有限公司', '爱尔康', 'aierkang', 'aek', '北京市' UNION ALL
SELECT '参天制药(中国)有限公司', '参天中国', 'cantianzhongguo', 'ctzg', '江苏省苏州市' UNION ALL
SELECT '武汉五景药业有限公司', '五景药业', 'wujingyaoye', 'wjyy', '湖北省武汉市' UNION ALL
SELECT '珠海联邦制药股份有限公司中山分公司', '联邦眼科', 'lianbangyanke', 'lbyk', '广东省中山市' UNION ALL
SELECT '江苏普华克胜药业有限公司', '普华克胜', 'puhuakesheng', 'phks', '江苏省连云港市' UNION ALL

-- ===== 维生素/营养保健 =====
SELECT '汤臣倍健股份有限公司', '汤臣倍健', 'tangchenbeijian', 'tcbj', '广东省珠海市' UNION ALL
SELECT '惠氏制药有限公司', '惠氏', 'huishi', 'hs', '上海市' UNION ALL
SELECT '北京康远制药有限公司', '康远制药', 'kangyuanzhiyao', 'kyzy', '北京市' UNION ALL
SELECT '石药集团维生药业有限公司', '石药维生', 'shiyaoweisheng', 'syws', '河北省石家庄市' UNION ALL
SELECT '华北制药河北华民药业有限责任公司', '华民药业', 'huaminyaoye', 'hmyy', '河北省石家庄市' UNION ALL
SELECT '杭州民生药业有限公司', '民生药业', 'minshengyaoye', 'msyy', '浙江省杭州市' UNION ALL
SELECT '浙江医药股份有限公司新昌制药厂', '新昌制药', 'xinchangzhiyao', 'xczy', '浙江省绍兴市' UNION ALL
SELECT '东北制药集团沈阳第一制药有限公司', '东药一厂', 'dongyaoyichang', 'dyyc', '辽宁省沈阳市' UNION ALL
SELECT '北京双吉制药有限公司', '双吉制药', 'shuangjizhiyao', 'sjzy', '北京市' UNION ALL
SELECT '西安利君制药有限责任公司', '利君制药', 'lijunzhiyao', 'ljzy', '陕西省西安市' UNION ALL

-- ===== 儿科药企 =====
SELECT '山东达因海洋生物制药股份有限公司', '达因制药', 'dayinzhiyao', 'dyzy', '山东省荣成市' UNION ALL
SELECT '深圳市儿童医院制剂室', '深圳儿童', 'shenzhenertong', 'szet', '广东省深圳市' UNION ALL
SELECT '丁桂(亚宝)药业集团股份有限公司', '亚宝药业', 'yabaoyaoye', 'ybyy', '山西省运城市' UNION ALL
SELECT '湖南方盛制药股份有限公司', '湖南方盛', 'hunanfangsheng', 'hnfs', '湖南省长沙市' UNION ALL
SELECT '哈药集团世一堂制药厂', '世一堂', 'shiyitang', 'syt', '黑龙江省哈尔滨市' UNION ALL
SELECT '武汉健民药业集团股份有限公司', '健民药业', 'jianminyaoye', 'jmyy', '湖北省武汉市' UNION ALL
SELECT '海南康芝药业股份有限公司', '康芝药业', 'kangzhiyaoye', 'kzyy', '海南省海口市' UNION ALL

-- ===== 其他常见药企 =====
SELECT '浙江花园生物高科股份有限公司', '花园生物', 'huayuanshengwu', 'hysw', '浙江省金华市' UNION ALL
SELECT '湖南尔康制药股份有限公司', '尔康制药', 'erkangzhiyao', 'ekzy', '湖南省长沙市' UNION ALL
SELECT '广东众生药业股份有限公司', '众生药业', 'zhongshengyaoye', 'zsyy', '广东省东莞市' UNION ALL
SELECT '贵州三力制药股份有限公司', '三力制药', 'sanlizhiyao', 'slzy', '贵州省贵阳市' UNION ALL
SELECT '长春海外制药集团有限公司', '海外制药', 'haiwaizhiyao', 'hwzy', '吉林省长春市' UNION ALL
SELECT '山西亚宝药业集团股份有限公司', '山西亚宝', 'shanxiyabao', 'sxyb', '山西省运城市' UNION ALL
SELECT '天津金耀集团有限公司', '金耀集团', 'jinyaojituan', 'jyjt', '天津市' UNION ALL
SELECT '苏州东瑞制药有限公司', '苏州东瑞', 'suzhoudongrui', 'szdr', '江苏省苏州市' UNION ALL
SELECT '成都地奥集团天府药业股份有限公司', '地奥天府', 'diaoianfu', 'datf', '四川省成都市' UNION ALL
SELECT '国药集团致君(深圳)制药有限公司', '致君制药', 'zhijunzhiyao', 'zjzy', '广东省深圳市' UNION ALL
SELECT '浙江尖峰药业有限公司', '尖峰药业', 'jianfengyaoye', 'jfyy', '浙江省金华市' UNION ALL
SELECT '江苏正大丰海制药有限公司', '正大丰海', 'zhengdafenghai', 'zdfh', '江苏省连云港市' UNION ALL
SELECT '南京正大天晴制药有限公司', '南京正大天晴', 'nanjingzhengdatianqing', 'njzdtq', '江苏省南京市' UNION ALL
SELECT '重庆药友制药有限责任公司', '药友制药', 'yaoyouzhiyao', 'yyzy', '重庆市' UNION ALL
SELECT '广东华南药业集团有限公司', '华南药业', 'huananyaoye', 'hnyy', '广东省广州市' UNION ALL
SELECT '四川升和药业股份有限公司', '升和药业', 'shengheyaoye', 'shyy', '四川省成都市'
) t
LEFT JOIN drug_manufacturer dm ON dm.name = t.name AND dm.deleted = 0
WHERE dm.id IS NULL;
