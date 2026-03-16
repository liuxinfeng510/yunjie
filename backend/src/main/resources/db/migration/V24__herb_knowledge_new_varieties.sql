-- =====================================================
-- V24: 新增中药知识库品种 (~75味补充中药)
-- 在V2的151味基础上补充常见中药品种
-- =====================================================

-- ==================== 解表药补充 ====================
INSERT INTO herb_knowledge (herb_code, herb_name, alias, pinyin, latin_name, source, origin, nature, flavor, meridian, efficacy, indication, dosage_min, dosage_max, dosage_usage, processing_method, contraindication, precaution, storage, is_toxic, toxic_level, is_precious, medical_insurance, identification) VALUES
('HK0152', '藁本', '香藁本', 'gaoben', 'Ligusticum sinense Oliv.', '伞形科植物藁本的干燥根茎和根', '四川、湖北、陕西', '温', '辛', '膀胱、肝', '祛风散寒，除湿止痛', '风寒感冒，巅顶疼痛，风湿痹痛', 3, 10, '煎服', '生用', '血虚头痛者忌用', '阴虚火旺者慎用', '密封干燥', 0, NULL, 0, '甲', '不规则结节状圆柱形，表面棕褐色或暗棕色'),
('HK0153', '辛夷', '木笔花', 'xinyi', 'Magnolia biondii Pamp.', '木兰科植物望春花的干燥花蕾', '河南、湖北、四川', '温', '辛', '肺、胃', '发散风寒，通鼻窍', '风寒头痛，鼻塞流涕，鼻渊鼻鼽', 3, 10, '煎服，包煎', '生用', '阴虚火旺者忌服', '包煎，以免毛刺激咽喉', '密封干燥', 0, NULL, 0, '甲', '长卵形，似毛笔头，表面密被灰白色或灰绿色茸毛'),
('HK0154', '苍耳子', '苍耳实', 'cangerzi', 'Xanthium sibiricum Patrin ex Widder', '菊科植物苍耳的干燥成熟带总苞的果实', '全国各地均产', '温', '辛、苦', '肺', '发散风寒，通鼻窍，祛风湿，止痛', '风寒头痛，鼻渊流涕，风疹瘙痒', 3, 10, '煎服', '炒去刺用', '血虚头痛者不宜', '有毒，不宜过量', '密封干燥', 1, '小毒', 0, '甲', '纺锤形或卵圆形，表面黄棕色或黄绿色，密被钩刺');

-- ==================== 清热药补充 ====================
INSERT INTO herb_knowledge (herb_code, herb_name, alias, pinyin, latin_name, source, origin, nature, flavor, meridian, efficacy, indication, dosage_min, dosage_max, dosage_usage, processing_method, contraindication, precaution, storage, is_toxic, toxic_level, is_precious, medical_insurance, identification) VALUES
('HK0155', '天花粉', '栝楼根', 'tianhuafen', 'Trichosanthes kirilowii Maxim.', '葫芦科植物栝楼的干燥根', '河南、山东、安徽', '微寒', '甘、微苦', '肺、胃', '清热泻火，生津止渴，消肿排脓', '热病烦渴，肺热燥咳，内热消渴', 10, 15, '煎服', '生用', '孕妇忌用。反乌头', '不宜与乌头类同用（十八反）', '密封干燥', 0, NULL, 0, '甲', '不规则圆柱形，表面黄白色或淡棕黄色'),
('HK0156', '决明子', '草决明', 'juemingzi', 'Cassia obtusifolia L.', '豆科植物决明的干燥成熟种子', '安徽、广西、四川', '微寒', '甘、苦、咸', '肝、大肠', '清热明目，润肠通便', '目赤涩痛，羞明多泪，头痛眩晕', 9, 15, '煎服', '生用或炒用', '脾虚便溏者忌服', '气虚便溏者慎用', '密封干燥', 0, NULL, 0, '甲', '略呈菱方形或短圆柱形，表面绿棕色或暗棕色'),
('HK0157', '青黛', '靛花', 'qingdai', 'Baphicacanthus cusia (Nees) Bremek.', '爵床科植物马蓝等叶经加工制得的干燥粉末或团块', '福建、云南、广西', '寒', '咸', '肝、肺', '清热解毒，凉血消斑，泻火定惊', '温毒发斑，血热吐衄，惊痫抽搐', 1, 3, '布包煎服，研末冲服每次1~3g', '生用', '脾胃虚寒者禁用', '用量宜小', '密封避光', 0, NULL, 0, '甲', '极细粉末，深蓝色，体轻易飞扬'),
('HK0158', '大青叶', '蓝叶', 'daqingye', 'Isatis indigotica Fort.', '十字花科植物菘蓝的干燥叶', '河北、安徽、江苏', '寒', '苦', '心、胃', '清热解毒，凉血消斑', '热病高热，温毒发斑，痄腮', 9, 15, '煎服', '生用', '脾胃虚寒者忌服', '非实热者不宜', '密封干燥', 0, NULL, 0, '甲', '多皱缩卷曲，有时破碎，暗灰绿色'),
('HK0159', '射干', '乌扇', 'shegan', 'Belamcanda chinensis (L.) DC.', '鸢尾科植物射干的干燥根茎', '湖北、河南、江苏', '寒', '苦', '肺', '清热解毒，消痰，利咽', '咽喉肿痛，痰涎壅盛，咳嗽气喘', 3, 10, '煎服', '生用', '脾虚便溏者不宜', '孕妇忌用', '密封干燥', 0, NULL, 0, '甲', '不规则结节状，偏平弯曲，表面黄褐色至棕褐色'),
('HK0160', '白头翁', '毛姑朵花', 'baitouweng', 'Pulsatilla chinensis (Bge.) Regel', '毛茛科植物白头翁的干燥根', '辽宁、安徽、河南', '寒', '苦', '胃、大肠', '清热解毒，凉血止痢', '热毒血痢，阴痒带下', 9, 15, '煎服', '生用', '虚寒泻痢者忌服', NULL, '密封干燥', 0, NULL, 0, '甲', '类圆柱形或圆锥形，表面黄棕色或棕褐色'),
('HK0161', '马齿苋', '五行草', 'machixian', 'Portulaca oleracea L.', '马齿苋科植物马齿苋的干燥地上部分', '全国各地均产', '寒', '酸', '肝、大肠', '清热解毒，凉血止血，止痢', '热毒血痢，热毒疮疡，崩漏便血', 9, 15, '煎服。鲜品30~60g', '生用', '脾胃虚寒者慎用', '孕妇忌用', '密封干燥', 0, NULL, 0, '甲', '全草多皱缩卷曲成团，茎圆柱形，紫红色');

-- ==================== 泻下药补充 ====================
INSERT INTO herb_knowledge (herb_code, herb_name, alias, pinyin, latin_name, source, origin, nature, flavor, meridian, efficacy, indication, dosage_min, dosage_max, dosage_usage, processing_method, contraindication, precaution, storage, is_toxic, toxic_level, is_precious, medical_insurance, identification) VALUES
('HK0162', '番泻叶', '泻叶', 'fanxieye', 'Cassia angustifolia Vahl', '豆科植物狭叶番泻的干燥小叶', '进口（印度、埃及）', '寒', '甘、苦', '大肠', '泻下通便', '热结积滞，便秘腹痛，水肿胀满', 2, 6, '后下或开水泡服', '生用', '孕妇、哺乳期妇女及月经期慎用', '不宜长期使用。大剂量可致恶心呕吐', '密封干燥', 0, NULL, 0, '甲', '长卵形或卵状披针形，全缘，叶面黄绿色'),
('HK0163', '郁李仁', '小李仁', 'yuliren', 'Prunus humilis Bge.', '蔷薇科植物欧李的干燥成熟种子', '辽宁、河北、内蒙古', '平', '辛、苦、甘', '脾、大肠、小肠', '润肠通便，下气利水', '大肠气滞，燥涩不通，小便不利', 6, 10, '煎服，捣碎入煎', '生用', '孕妇慎用', NULL, '密封干燥', 0, NULL, 0, '甲', '卵形，表面黄白色或浅棕色');

-- ==================== 祛风湿药补充 ====================
INSERT INTO herb_knowledge (herb_code, herb_name, alias, pinyin, latin_name, source, origin, nature, flavor, meridian, efficacy, indication, dosage_min, dosage_max, dosage_usage, processing_method, contraindication, precaution, storage, is_toxic, toxic_level, is_precious, medical_insurance, identification) VALUES
('HK0164', '木瓜', '贴梗海棠', 'mugua', 'Chaenomeles speciosa (Sweet) Nakai', '蔷薇科植物贴梗海棠的干燥近成熟果实', '安徽（宣城）、四川、湖北', '温', '酸', '肝、脾', '舒筋活络，和胃化湿', '风湿痹痛，筋脉拘挛，吐泻转筋', 6, 12, '煎服', '生用', '内有郁热、小便短赤者忌服', NULL, '密封干燥', 0, NULL, 0, '甲', '长圆形，多纵剖为两半，外表面紫红色或红棕色'),
('HK0165', '防己', '汉防己', 'fangji', 'Stephania tetrandra S.Moore', '防己科植物粉防己的干燥根', '浙江、安徽、湖北', '寒', '苦、辛', '膀胱、肺', '祛风止痛，利水消肿', '风湿痹痛，水肿脚气，小便不利', 5, 10, '煎服', '生用', '阴虚无湿热者及食欲不振者慎用', '广防己含马兜铃酸有毒，不可混用', '密封干燥', 0, NULL, 0, '甲', '不规则圆柱形，表面淡灰黄色，有弯曲沟纹'),
('HK0166', '豨莶草', '猪膏母', 'xixiancao', 'Siegesbeckia orientalis L.', '菊科植物豨莶的干燥地上部分', '全国各地均产', '寒', '辛、苦', '肝、肾', '祛风湿，利关节，解毒', '风湿痹痛，筋骨无力，腰膝酸软', 9, 12, '煎服', '生用或酒制。酒制祛风湿力强', '血虚者不宜', NULL, '密封干燥', 0, NULL, 0, '甲', '茎略呈方柱形，表面灰绿色或黄绿色');

-- ==================== 化湿药补充 ====================
INSERT INTO herb_knowledge (herb_code, herb_name, alias, pinyin, latin_name, source, origin, nature, flavor, meridian, efficacy, indication, dosage_min, dosage_max, dosage_usage, processing_method, contraindication, precaution, storage, is_toxic, toxic_level, is_precious, medical_insurance, identification) VALUES
('HK0167', '白豆蔻', '白蔻', 'baidoukou', 'Amomum kravanh Pierre ex Gagnep.', '姜科植物白豆蔻的干燥成熟果实', '进口（泰国、柬埔寨）', '温', '辛', '肺、脾、胃', '化湿行气，温中止呕', '湿浊中阻，不思饮食，湿温初起', 3, 6, '煎服，后下', '生用', '阴虚血燥者禁用', '后下，不宜久煎', '密封干燥', 0, NULL, 0, '甲', '类球形，表面黄白色至淡黄棕色'),
('HK0168', '草果', '草果仁', 'caoguo', 'Amomum tsao-ko Crevost et Lemaire', '姜科植物草果的干燥成熟果实', '云南、广西、贵州', '温', '辛', '脾、胃', '燥湿温中，截疟除痰', '寒湿中阻，脘腹胀痛，疟疾', 3, 6, '煎服', '姜汁炒用', '气虚血亏者忌服', NULL, '密封干燥', 0, NULL, 0, '甲', '长椭圆形，表面棕褐色或红棕色');

-- ==================== 利水渗湿药补充 ====================
INSERT INTO herb_knowledge (herb_code, herb_name, alias, pinyin, latin_name, source, origin, nature, flavor, meridian, efficacy, indication, dosage_min, dosage_max, dosage_usage, processing_method, contraindication, precaution, storage, is_toxic, toxic_level, is_precious, medical_insurance, identification) VALUES
('HK0169', '通草', '通脱木', 'tongcao', 'Tetrapanax papyrifer (Hook.) K.Koch', '五加科植物通脱木的干燥茎髓', '贵州、四川、云南', '微寒', '甘、淡', '肺、胃', '清热利尿，通气下乳', '湿热淋证，水肿尿少，乳汁不下', 3, 5, '煎服', '生用', '气阴两虚者及孕妇慎用', NULL, '密封干燥', 0, NULL, 0, '甲', '圆柱形，表面白色或淡黄色，体轻'),
('HK0170', '海金沙', '竹园荽', 'haijinsha', 'Lygodium japonicum (Thunb.) Sw.', '海金沙科植物海金沙的干燥成熟孢子', '浙江、江苏、广东', '寒', '甘、淡', '膀胱、小肠', '清利湿热，通淋止痛', '热淋石淋，血淋膏淋，尿道涩痛', 6, 15, '煎服，包煎', '生用', '肾阴虚者忌服', '包煎', '密封干燥', 0, NULL, 0, '甲', '粉末状，棕黄色至浅棕色，体轻滑手'),
('HK0171', '萹蓄', '萹竹', 'bianxu', 'Polygonum aviculare L.', '蓼科植物萹蓄的干燥地上部分', '全国各地均产', '微寒', '苦', '膀胱', '利尿通淋，杀虫止痒', '热淋涩痛，小便短赤，虫积腹痛', 9, 15, '煎服', '生用', '脾胃虚弱者慎用', NULL, '密封干燥', 0, NULL, 0, '甲', '茎圆柱形而略扁，节膨大');

-- ==================== 理气药补充 ====================
INSERT INTO herb_knowledge (herb_code, herb_name, alias, pinyin, latin_name, source, origin, nature, flavor, meridian, efficacy, indication, dosage_min, dosage_max, dosage_usage, processing_method, contraindication, precaution, storage, is_toxic, toxic_level, is_precious, medical_insurance, identification) VALUES
('HK0172', '青皮', '四花青皮', 'qingpi', 'Citrus reticulata Blanco', '芸香科植物橘的干燥幼果或未成熟果实的果皮', '福建、四川、浙江', '温', '苦、辛', '肝、胆、胃', '疏肝破气，消积化滞', '胸胁胀痛，疝气疼痛，乳癖乳痈', 3, 10, '煎服', '生用或醋炙', '气虚者慎用', NULL, '密封干燥', 0, NULL, 0, '甲', '四花青皮呈四瓣状，个青皮呈球形'),
('HK0173', '乌药', '天台乌药', 'wuyao', 'Lindera aggregata (Sims) Kosterm.', '樟科植物乌药的干燥块根', '浙江（天台）、安徽、湖南', '温', '辛', '肺、脾、肾、膀胱', '行气止痛，温肾散寒', '寒凝气滞，胸腹胀痛，膀胱虚冷', 6, 10, '煎服', '生用', '气虚内热者忌服', NULL, '密封干燥', 0, NULL, 0, '甲', '纺锤状，表面黄棕色或黄褐色'),
('HK0174', '薤白', '小蒜', 'xiebai', 'Allium macrostemon Bge.', '百合科植物小根蒜的干燥鳞茎', '东北、河北、江苏', '温', '辛、苦', '心、肺、胃、大肠', '通阳散结，行气导滞', '胸痹心痛，脘腹痞满，泻痢后重', 5, 10, '煎服', '生用', '气虚无滞者慎用', NULL, '密封干燥', 0, NULL, 0, '甲', '呈不规则卵圆形，表面黄白色或淡黄棕色'),
('HK0175', '川楝子', '金铃子', 'chuanlianzi', 'Melia toosendan Sieb. et Zucc.', '楝科植物川楝的干燥成熟果实', '四川、贵州、湖南', '寒', '苦', '肝、小肠、膀胱', '疏肝泄热，行气止痛，杀虫', '肝郁化火，胸胁脘腹胀痛，虫积腹痛', 5, 10, '煎服', '生用或炒用', '脾胃虚寒者不宜', '有小毒，不宜过量', '密封干燥', 1, '小毒', 0, '甲', '类球形，表面金黄色至棕黄色');

-- ==================== 止血药补充 ====================
INSERT INTO herb_knowledge (herb_code, herb_name, alias, pinyin, latin_name, source, origin, nature, flavor, meridian, efficacy, indication, dosage_min, dosage_max, dosage_usage, processing_method, contraindication, precaution, storage, is_toxic, toxic_level, is_precious, medical_insurance, identification) VALUES
('HK0176', '仙鹤草', '龙芽草', 'xianhecao', 'Agrimonia pilosa Ledeb.', '蔷薇科植物龙芽草的干燥地上部分', '全国各地均产', '平', '苦、涩', '心、肝', '收敛止血，截疟，止痢，解毒，补虚', '咯血吐血，崩漏便血，疟疾痢疾', 6, 12, '煎服', '生用', NULL, NULL, '密封干燥', 0, NULL, 0, '甲', '茎圆柱形，表面灰绿色或棕绿色'),
('HK0177', '地榆', '黄瓜香', 'diyu', 'Sanguisorba officinalis L.', '蔷薇科植物地榆的干燥根', '黑龙江、吉林、内蒙古', '微寒', '苦、酸、涩', '肝、大肠', '凉血止血，解毒敛疮', '血热便血，痔血崩漏，水火烫伤', 9, 15, '煎服。外用适量', '生用或炒炭', '大面积烧伤不宜外用', '虚寒性出血不宜', '密封干燥', 0, NULL, 0, '甲', '不规则纺锤形或圆柱形，表面灰褐色或暗紫色'),
('HK0178', '槐花', '槐米', 'huaihua', 'Sophora japonica L.', '豆科植物槐的干燥花及花蕾', '全国各地均产', '微寒', '苦', '肝、大肠', '凉血止血，清肝泻火', '便血痔血，血痢崩漏，肝热目赤', 5, 10, '煎服', '生用、炒用或炒炭', '脾胃虚寒及阴虚发热者忌服', NULL, '密封干燥', 0, NULL, 0, '甲', '花蕾（槐米）卵形或椭圆形，花萼钟状'),
('HK0179', '侧柏叶', '柏叶', 'cebaiye', 'Platycladus orientalis (L.) Franco', '柏科植物侧柏的干燥枝梢和叶', '全国各地均产', '微寒', '苦、涩', '肺、肝、脾', '凉血止血，化痰止咳，生发乌发', '吐血衄血，咯血便血，崩漏不止', 6, 12, '煎服。外用适量', '生用或炒炭', '虚寒出血者慎用', NULL, '密封干燥', 0, NULL, 0, '甲', '枝梢多分枝，小枝扁平'),
('HK0180', '小蓟', '猫蓟', 'xiaoji', 'Cirsium setosum (Willd.) MB.', '菊科植物刺儿菜的干燥地上部分', '全国各地均产', '凉', '甘、苦', '心、肝', '凉血止血，散瘀解毒消痈', '衄血吐血，尿血血淋，疮痈肿毒', 5, 12, '煎服', '生用或炒炭', '脾胃虚寒者不宜', NULL, '密封干燥', 0, NULL, 0, '甲', '全草缠绕成团，茎圆柱形'),
('HK0181', '大蓟', '马蓟', 'daji', 'Cirsium japonicum DC.', '菊科植物蓟的干燥地上部分或根', '全国各地均产', '凉', '甘、苦', '心、肝', '凉血止血，散瘀解毒消痈', '衄血吐血，咯血咳血，疮痈肿毒', 9, 15, '煎服。外用适量', '生用或炒炭', '脾胃虚寒而无瘀滞者慎用', NULL, '密封干燥', 0, NULL, 0, '甲', '茎圆柱形，表面绿色或棕绿色');

-- ==================== 活血祛瘀药补充 ====================
INSERT INTO herb_knowledge (herb_code, herb_name, alias, pinyin, latin_name, source, origin, nature, flavor, meridian, efficacy, indication, dosage_min, dosage_max, dosage_usage, processing_method, contraindication, precaution, storage, is_toxic, toxic_level, is_precious, medical_insurance, identification) VALUES
('HK0182', '乳香', '滴乳香', 'ruxiang', 'Boswellia carterii Birdw.', '橄榄科植物卡氏乳香树的含油脂树脂', '进口（索马里、埃塞俄比亚）', '温', '辛、苦', '心、肝、脾', '活血定痛，消肿生肌', '胸痹心痛，胃脘疼痛，痛经闭经', 3, 10, '煎服', '生用或炒用', '孕妇忌服', '胃弱者慎用', '密封干燥', 0, NULL, 0, '甲', '呈长卵形滴乳状、类圆形颗粒，淡黄色至黄棕色'),
('HK0183', '没药', '末药', 'moyao', 'Commiphora myrrha Engl.', '橄榄科植物地丁树的干燥树脂', '进口（索马里、埃塞俄比亚）', '平', '辛、苦', '心、肝、脾', '散瘀定痛，消肿生肌', '胸痹心痛，胃脘疼痛，痛经经闭', 3, 10, '煎服', '生用或炒用', '孕妇忌服', '胃弱者慎用', '密封干燥', 0, NULL, 0, '甲', '不规则颗粒状或粘结块状，表面棕黄色至棕褐色'),
('HK0184', '姜黄', '片姜黄', 'jianghuang', 'Curcuma longa L.', '姜科植物姜黄的干燥根茎', '四川、福建', '温', '辛、苦', '脾、肝', '破血行气，通经止痛', '胸胁刺痛，胸痹心痛，痛经经闭', 3, 10, '煎服', '生用', '血虚无气滞血瘀者及孕妇忌用', NULL, '密封干燥', 0, NULL, 0, '甲', '不规则卵圆形或圆柱形，表面深黄色'),
('HK0185', '鸡血藤', '血风藤', 'jixueteng', 'Spatholobus suberectus Dunn', '豆科植物密花豆的干燥藤茎', '广西、云南、贵州', '温', '苦、甘', '肝、肾', '活血补血，调经止痛，舒筋活络', '月经不调，痛经经闭，风湿痹痛', 9, 15, '煎服', '生用', NULL, NULL, '密封干燥', 0, NULL, 0, '甲', '扁圆柱形，切面有数个同心性椭圆形环，棕色树脂样物与淡色木质部相间排列'),
('HK0186', '泽兰', '地瓜儿苗', 'zelan', 'Lycopus lucidus Turcz.', '唇形科植物毛叶地瓜儿苗的干燥地上部分', '江苏、安徽、四川', '微温', '苦、辛', '肝、脾', '活血调经，祛瘀消痈，利水消肿', '月经不调，经闭痛经，产后瘀滞', 6, 12, '煎服', '生用', '无瘀血者慎用', NULL, '密封干燥', 0, NULL, 0, '甲', '茎方柱形，表面黄绿色或绿紫色'),
('HK0187', '王不留行', '王不留', 'wangbuliuxing', 'Vaccaria segetalis (Neck.) Garcke', '石竹科植物麦蓝菜的干燥成熟种子', '河北、山东、辽宁', '平', '苦', '肝、胃', '活血通经，下乳消肿，利尿通淋', '经闭痛经，乳汁不下，淋证涩痛', 5, 10, '煎服', '生用或炒用', '孕妇禁用，月经过多者不宜', NULL, '密封干燥', 0, NULL, 0, '甲', '球形，表面黑色，有光泽');

-- ==================== 化痰止咳药补充 ====================
INSERT INTO herb_knowledge (herb_code, herb_name, alias, pinyin, latin_name, source, origin, nature, flavor, meridian, efficacy, indication, dosage_min, dosage_max, dosage_usage, processing_method, contraindication, precaution, storage, is_toxic, toxic_level, is_precious, medical_insurance, identification) VALUES
('HK0188', '白芥子', '芥菜子', 'baijiezi', 'Sinapis alba L.', '十字花科植物白芥的干燥成熟种子', '安徽、河南、四川', '温', '辛', '肺', '温肺豁痰利气，散结通络止痛', '寒痰喘咳，胸胁胀痛，痰滞经络', 3, 6, '煎服', '生用或炒用', '肺虚咳嗽及阴虚火旺者忌服', NULL, '密封干燥', 0, NULL, 0, '甲', '球形，表面灰白色至淡黄色'),
('HK0189', '瓜蒌', '栝楼', 'gualou', 'Trichosanthes kirilowii Maxim.', '葫芦科植物栝楼的干燥成熟果实', '山东、河南、安徽', '寒', '甘、微苦', '肺、胃、大肠', '清热涤痰，宽胸散结，润燥滑肠', '痰热咳嗽，胸痹心痛，结胸痞满', 9, 15, '煎服', '全瓜蒌、瓜蒌皮、瓜蒌仁分用', '脾虚便溏及寒痰湿痰者不宜', '反乌头（十八反）', '密封干燥', 0, NULL, 0, '甲', '类球形或宽椭圆形，表面橙红色或橙黄色'),
('HK0190', '竹茹', '竹二青', 'zhuru', 'Bambusa tuldoides Munro', '禾本科植物青秆竹的茎秆的干燥中间层', '广东、四川、湖南', '微寒', '甘', '肺、胃、心、胆', '清热化痰，除烦止呕', '痰热咳嗽，胆火挟痰，惊悸不宁', 5, 10, '煎服', '生用或姜汁炙', '脾胃虚寒者慎用', NULL, '密封干燥', 0, NULL, 0, '甲', '卷曲成团的不规则丝条状，淡绿黄色'),
('HK0191', '旋覆花', '金沸草', 'xuanfuhua', 'Inula japonica Thunb.', '菊科植物旋覆花的干燥头状花序', '河南、江苏、浙江', '微温', '苦、辛、咸', '肺、胃、大肠', '降气消痰，行水止呕', '风寒咳嗽，痰饮蓄结，胸膈痞闷', 3, 10, '煎服，包煎', '生用', '阴虚劳嗽及大便泄泻者慎用', '包煎，以免毛刺激咽喉', '密封干燥', 0, NULL, 0, '甲', '扁球形或类球形，总苞片多层');

-- ==================== 安神药补充 ====================
INSERT INTO herb_knowledge (herb_code, herb_name, alias, pinyin, latin_name, source, origin, nature, flavor, meridian, efficacy, indication, dosage_min, dosage_max, dosage_usage, processing_method, contraindication, precaution, storage, is_toxic, toxic_level, is_precious, medical_insurance, identification) VALUES
('HK0192', '合欢皮', '夜合皮', 'hehuanpi', 'Albizia julibrissin Durazz.', '豆科植物合欢的干燥树皮', '湖北、四川、安徽', '平', '甘', '心、肝、肺', '解郁安神，活血消肿', '心神不安，忧郁失眠，跌扑伤痛', 6, 12, '煎服', '生用', '孕妇慎用', NULL, '密封干燥', 0, NULL, 0, '甲', '筒状或槽状，外表面灰棕色至灰褐色'),
('HK0193', '夜交藤', '首乌藤', 'yejiaoteng', 'Polygonum multiflorum Thunb.', '蓼科植物何首乌的干燥藤茎', '湖北、河南、四川', '平', '甘', '心、肝', '养血安神，祛风通络', '失眠多梦，血虚身痛，风湿痹痛', 9, 15, '煎服', '生用', '燥热者慎用', NULL, '密封干燥', 0, NULL, 0, '甲', '长圆柱形，表面紫褐色或棕褐色');

-- ==================== 补气药补充 ====================
INSERT INTO herb_knowledge (herb_code, herb_name, alias, pinyin, latin_name, source, origin, nature, flavor, meridian, efficacy, indication, dosage_min, dosage_max, dosage_usage, processing_method, contraindication, precaution, storage, is_toxic, toxic_level, is_precious, medical_insurance, identification) VALUES
('HK0194', '黄精', '鸡头黄精', 'huangjing', 'Polygonatum kingianum Coll. et Hemsl.', '百合科植物滇黄精的干燥根茎', '贵州、云南、四川', '平', '甘', '脾、肺、肾', '补气养阴，健脾，润肺，益肾', '脾胃气虚，体倦乏力，口干食少', 9, 15, '煎服', '生用或制用（酒蒸）', '中寒泄泻及痰湿痞满者忌服', NULL, '密封干燥', 0, NULL, 0, '甲', '不规则结节状，表面淡黄色至黄棕色'),
('HK0195', '白扁豆', '南扁豆', 'baibiandou', 'Dolichos lablab L.', '豆科植物扁豆的干燥成熟种子', '云南、河南、安徽', '微温', '甘', '脾、胃', '健脾化湿，和中消暑', '脾胃虚弱，食欲不振，暑湿吐泻', 9, 15, '煎服', '生用或炒用', NULL, '炒用健脾止泻力强', '密封干燥', 0, NULL, 0, '甲', '扁椭圆形或扁卵圆形，表面淡黄白色或淡黄色');

-- ==================== 补阴药补充 ====================
INSERT INTO herb_knowledge (herb_code, herb_name, alias, pinyin, latin_name, source, origin, nature, flavor, meridian, efficacy, indication, dosage_min, dosage_max, dosage_usage, processing_method, contraindication, precaution, storage, is_toxic, toxic_level, is_precious, medical_insurance, identification) VALUES
('HK0196', '北沙参', '莱阳参', 'beishashen', 'Glehnia littoralis F.Schmidt ex Miq.', '伞形科植物珊瑚菜的干燥根', '山东（莱阳）、河北、辽宁', '微寒', '甘、微苦', '肺、胃', '养阴清肺，益胃生津', '肺热燥咳，劳嗽痰血，胃阴不足', 5, 12, '煎服', '生用', '风寒咳嗽者禁服', '反藜芦', '密封干燥', 0, NULL, 0, '甲', '细长圆柱形，表面淡黄白色'),
('HK0197', '南沙参', '泡参', 'nanshashen', 'Adenophora tetraphylla (Thunb.) Fisch.', '桔梗科植物轮叶沙参的干燥根', '安徽、江苏、浙江', '微寒', '甘', '肺、胃', '养阴清肺，益胃生津，化痰，益气', '肺热燥咳，阴虚久咳，胃阴不足', 9, 15, '煎服', '生用', '风寒咳嗽者忌服', '反藜芦', '密封干燥', 0, NULL, 0, '甲', '圆锥形或圆柱形，表面黄白色或淡棕黄色'),
('HK0198', '龟甲', '龟板', 'guijia', 'Chinemys reevesii (Gray)', '龟科动物乌龟的腹甲及背甲', '湖北、安徽、湖南', '微寒', '甘、咸', '肝、肾、心', '滋阴潜阳，益肾强骨，养血补心，固经止崩', '阴虚阳亢，骨蒸潮热，阴虚风动', 9, 24, '煎服，先煎', '生用或醋炙。砂炒后先煎', '孕妇及胃有寒湿者忌服', '先煎', '密封干燥', 0, NULL, 1, '甲', '背甲及腹甲由角质板和骨板组成'),
('HK0199', '鳖甲', '团鱼壳', 'biejia', 'Trionyx sinensis Wiegmann', '鳖科动物鳖的背甲', '湖北、安徽、湖南', '微寒', '咸', '肝、肾', '滋阴潜阳，退热除蒸，软坚散结', '阴虚发热，骨蒸劳热，阴虚阳亢', 9, 24, '煎服，先煎', '醋炙。砂炒后先煎', '孕妇及脾胃阳虚者忌服', '先煎', '密封干燥', 0, NULL, 0, '甲', '卵圆形或椭圆形扁平板状，背面隆起');

-- ==================== 补阳药补充 ====================
INSERT INTO herb_knowledge (herb_code, herb_name, alias, pinyin, latin_name, source, origin, nature, flavor, meridian, efficacy, indication, dosage_min, dosage_max, dosage_usage, processing_method, contraindication, precaution, storage, is_toxic, toxic_level, is_precious, medical_insurance, identification) VALUES
('HK0200', '补骨脂', '破故纸', 'buguzi', 'Psoralea corylifolia L.', '豆科植物补骨脂的干燥成熟果实', '四川、河南、安徽', '温', '辛、苦', '肾、脾', '温肾助阳，纳气平喘，温脾止泻', '阳痿遗精，腰膝冷痛，五更泄泻', 6, 10, '煎服', '生用或盐水炙', '阴虚火旺者忌服', NULL, '密封干燥', 0, NULL, 0, '甲', '肾形，略扁，表面黑色或棕黑色'),
('HK0201', '益智仁', '益智子', 'yizhiren', 'Alpinia oxyphylla Miq.', '姜科植物益智的干燥成熟果实', '广东、海南、广西', '温', '辛', '脾、肾', '暖肾固精缩尿，温脾止泻摄涎', '肾虚遗尿，小便频数，脾寒泄泻', 3, 10, '煎服', '生用或盐水炙', '阴虚火旺者忌用', NULL, '密封干燥', 0, NULL, 0, '甲', '椭圆形，两端略尖，表面棕色或灰棕色'),
('HK0202', '骨碎补', '猴姜', 'gusuibu', 'Drynaria fortunei (Kunze) J.Sm.', '水龙骨科植物槲蕨的干燥根茎', '浙江、湖北、广东', '温', '苦', '肾、肝', '疗伤止痛，补肾强骨，续伤止痛', '跌扑闪挫，筋骨折伤，肾虚腰痛', 3, 10, '煎服', '砂烫去毛', '阴虚内热者慎用', NULL, '密封干燥', 0, NULL, 0, '甲', '扁平长条形，表面密被深棕色小鳞片'),
('HK0203', '蛤蚧', '对蛤蚧', 'gejie', 'Gekko gecko L.', '壁虎科动物蛤蚧的干燥体', '广西', '平', '咸', '肺、肾', '补肺益肾，纳气定喘，助阳益精', '肺肾虚喘，咳嗽气促，阳痿遗精', 3, 6, '研末冲服每次1~2g，或酒浸服', '生用或酒浸', '外感风寒咳嗽者忌服', '有小毒，外感喘嗽不宜', '密封干燥', 0, NULL, 1, '甲', '头部呈三角形，体和尾均具细鳞');

-- ==================== 收涩药补充 ====================
INSERT INTO herb_knowledge (herb_code, herb_name, alias, pinyin, latin_name, source, origin, nature, flavor, meridian, efficacy, indication, dosage_min, dosage_max, dosage_usage, processing_method, contraindication, precaution, storage, is_toxic, toxic_level, is_precious, medical_insurance, identification) VALUES
('HK0204', '乌梅', '梅实', 'wumei', 'Prunus mume (Sieb.) Sieb. et Zucc.', '蔷薇科植物梅的干燥近成熟果实', '四川、浙江、福建', '平', '酸、涩', '肝、脾、肺、大肠', '敛肺止咳，涩肠止泻，安蛔止痛，生津止渴', '肺虚久咳，久泻久痢，蛔厥腹痛', 6, 12, '煎服', '生用或炒炭', '外有表邪或内有实热积滞者不宜', NULL, '密封干燥', 0, NULL, 0, '甲', '类球形或扁球形，表面乌黑色或棕黑色'),
('HK0205', '金樱子', '刺梨子', 'jinyingzi', 'Rosa laevigata Michx.', '蔷薇科植物金樱子的干燥成熟果实', '广东、四川、云南', '平', '酸、甘、涩', '肾、膀胱、大肠', '固精缩尿，涩肠止泻', '遗精滑精，遗尿尿频，崩漏带下', 6, 12, '煎服', '生用', '有实火实邪者忌服', NULL, '密封干燥', 0, NULL, 0, '甲', '花托倒卵形，表面红黄色至红棕色'),
('HK0206', '覆盆子', '覆盆', 'fupenzi', 'Rubus chingii Hu', '蔷薇科植物华东覆盆子的干燥果实', '浙江、福建、安徽', '微温', '甘、酸', '肝、肾、膀胱', '益肾固精缩尿，养肝明目', '遗精滑精，遗尿尿频，肝肾不足', 6, 12, '煎服', '生用', '肾虚有火、小便短涩者慎服', NULL, '密封干燥', 0, NULL, 0, '甲', '聚合果，由多数小果聚合而成，呈圆锥形'),
('HK0207', '浮小麦', '浮水麦', 'fuxiaomai', 'Triticum aestivum L.', '禾本科植物小麦的干瘪轻浮的颖果', '全国各地均产', '凉', '甘', '心', '固表止汗，益气，除热', '自汗盗汗，骨蒸劳热', 15, 30, '煎服', '生用或炒用', '无汗热者不宜', NULL, '密封干燥', 0, NULL, 0, '甲', '长椭圆形，表面浅黄色至黄棕色，体轻能浮于水面');

-- ==================== 开窍药/外用药补充 ====================
INSERT INTO herb_knowledge (herb_code, herb_name, alias, pinyin, latin_name, source, origin, nature, flavor, meridian, efficacy, indication, dosage_min, dosage_max, dosage_usage, processing_method, contraindication, precaution, storage, is_toxic, toxic_level, is_precious, medical_insurance, identification) VALUES
('HK0208', '冰片', '龙脑', 'bingpian', 'Dryobalanops aromatica Gaertn.f.', '龙脑香科植物龙脑香的树脂加工品或人工合成', '进口或合成', '微寒', '辛、苦', '心、脾、肺', '开窍醒神，清热止痛', '热病神昏，惊厥，中风痰厥', 0.15, 0.3, '入丸散用。不入煎剂。外用适量', '研细末用', '孕妇慎用', '不入煎剂', '密封避光', 0, NULL, 0, '甲', '半透明结晶，片状、针状或颗粒状，白色'),
('HK0209', '石菖蒲', '九节菖蒲', 'shichangpu', 'Acorus tatarinowii Schott', '天南星科植物石菖蒲的干燥根茎', '四川、浙江、江苏', '温', '辛、苦', '心、胃', '开窍豁痰，醒神益智，化湿开胃', '神昏癫痫，健忘失眠，耳鸣耳聋', 3, 10, '煎服', '生用', '阴虚阳亢、烦躁汗多者忌服', NULL, '密封干燥', 0, NULL, 0, '甲', '扁圆柱形，稍弯曲，表面棕褐色或灰棕色'),
('HK0210', '人工牛黄', '牛黄', 'rengongniuhuang', 'Calculus Bovis Artifactus', '按牛黄成分人工配制的粉末', '人工合成', '凉', '甘', '心、肝', '清心豁痰，开窍凉肝，息风解毒', '热病神昏，中风痰迷，惊痫抽搐', 0.15, 0.35, '入丸散用。多入丸散，不入煎剂', '研末用', '孕妇慎用', '非实热者不宜', '密封遮光', 0, NULL, 0, '甲', '黄色或浅棕色粉末'),
('HK0211', '朱砂', '丹砂', 'zhusha', 'Cinnabaris (HgS)', '硫化物类矿物辰砂族辰砂的矿石', '贵州、湖南、四川', '微寒', '甘', '心', '清心镇惊，安神，明目，解毒', '心悸易惊，失眠多梦，癫痫发狂', 0.1, 0.5, '研末冲服。不入煎剂', '研细水飞', '不宜久服多服，肝肾功能不全者禁用', '含汞，有毒，不入煎剂，忌火煅', '密封遮光', 1, '有毒', 0, '甲', '粒状或块状集合体，鲜红色或暗红色');

-- ==================== 驱虫药/其他补充 ====================
INSERT INTO herb_knowledge (herb_code, herb_name, alias, pinyin, latin_name, source, origin, nature, flavor, meridian, efficacy, indication, dosage_min, dosage_max, dosage_usage, processing_method, contraindication, precaution, storage, is_toxic, toxic_level, is_precious, medical_insurance, identification) VALUES
('HK0212', '使君子', '留求子', 'shijunzi', 'Quisqualis indica L.', '使君子科植物使君子的干燥成熟果实', '广东、四川、广西', '温', '甘', '脾、胃', '杀虫消积', '蛔虫腹痛，小儿疳积', 9, 12, '煎服。取仁嚼服，小儿每岁1~1.5粒', '去壳取仁', '不宜大量服用', '不宜与热茶同服', '密封干燥', 0, NULL, 0, '甲', '椭圆形或卵圆形，外壳黑褐色，有5条纵棱'),
('HK0213', '槟榔', '大腹子', 'binglang', 'Areca catechu L.', '棕榈科植物槟榔的干燥成熟种子', '海南、云南、台湾', '温', '苦、辛', '胃、大肠', '杀虫消积，降气行水，截疟', '绦虫蛔虫，虫积腹痛，水肿脚气', 3, 10, '煎服。单用驱绦虫60~120g', '生用或炒用', '脾虚便溏者慎用', '气虚下陷者忌服', '密封干燥', 0, NULL, 0, '甲', '扁球形或圆锥形，表面淡黄棕色或淡红棕色'),
('HK0214', '苦楝皮', '楝根皮', 'kulianpi', 'Melia azedarach L.', '楝科植物楝树的干燥树皮和根皮', '四川、湖北、安徽', '寒', '苦', '肝、脾、胃', '杀虫，疗癣', '蛔虫腹痛，蛲虫，头癣', 3, 6, '煎服。外用适量', '生用', '肝肾功能不全者及孕妇忌服', '有毒，不宜过量或持续服用', '密封干燥', 1, '有毒', 0, '甲', '不规则板片状或槽状，外表面灰棕色或灰褐色'),
('HK0215', '桑螵蛸', '螳螂子', 'sangpiaoxiao', 'Tenodera sinensis Saussure', '螳螂科昆虫大刀螳的干燥卵鞘', '山东、浙江、湖北', '平', '甘、咸', '肝、肾', '固精缩尿，补肾助阳', '遗精滑精，遗尿尿频，小便白浊', 5, 10, '煎服', '蒸制后用', '阴虚火旺者及膀胱湿热者忌服', NULL, '密封干燥', 0, NULL, 0, '甲', '略呈圆柱形或半圆形，表面浅黄褐色'),
('HK0216', '水蛭', '蚂蟥', 'shuizhi', 'Whitmania pigra Whitman', '水蛭科动物蚂蟥的干燥全体', '山东、安徽、江苏', '平', '咸、苦', '肝', '破血通经，逐瘀消癥', '血瘀经闭，癥瘕痞块，中风偏瘫', 1, 3, '煎服。研末吞服每次0.3~0.5g', '滑石粉烫制', '孕妇及月经过多者禁用', '有毒，不宜过量', '密封干燥', 1, '小毒', 0, '甲', '扁平纺锤形，表面黑棕色或黑褐色');
