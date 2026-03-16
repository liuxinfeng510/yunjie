-- =====================================================
-- V23: 补充中药知识库空字段 (151味现有中药)
-- 补充: herb_code, latin_name, source, origin,
--       dosage_usage, dosage_note, processing_method,
--       contraindication, precaution, incompatibility,
--       identification, is_precious, medical_insurance
-- =====================================================

-- ==================== 解表药 ====================

UPDATE herb_knowledge SET
  herb_code = 'HK0001',
  latin_name = 'Ephedra sinica Stapf',
  source = '麻黄科植物草麻黄的干燥草质茎',
  origin = '内蒙古、甘肃、河北',
  dosage_usage = '煎服，先煎去沫',
  dosage_note = '发汗力强，用量不宜过大',
  processing_method = '生用或蜜炙。蜜炙偏于润肺止咳',
  contraindication = '体虚自汗、阴虚盗汗及肺肾虚喘者禁用',
  precaution = '表虚自汗、阴虚盗汗慎用，不宜与降压药合用',
  incompatibility = NULL,
  identification = '细长圆柱形，少分枝，直径1~2mm，表面淡绿色至黄绿色，有细纵脊线，节间长2~6cm',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '麻黄' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0002',
  latin_name = 'Cinnamomum cassia Presl',
  source = '樟科植物肉桂的干燥嫩枝',
  origin = '广西、广东、云南',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用',
  contraindication = '温热病及阴虚火旺者禁用',
  precaution = '孕妇及月经过多者慎用',
  incompatibility = NULL,
  identification = '长圆柱形，多分枝，表面红棕色至棕色，有纵棱线及细皱纹',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '桂枝' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0003',
  latin_name = 'Perilla frutescens (L.) Britt.',
  source = '唇形科植物紫苏的干燥叶',
  origin = '江苏、浙江、湖北',
  dosage_usage = '煎服，不宜久煎',
  dosage_note = '后下，煎煮时间不超过10分钟',
  processing_method = '生用',
  contraindication = '气虚表虚者慎用',
  precaution = '不宜久煎，以免挥发油散失',
  incompatibility = NULL,
  identification = '叶片多皱缩卷曲，两面紫色或上面绿色下面紫色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '紫苏叶' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0004',
  latin_name = 'Schizonepeta tenuifolia Briq.',
  source = '唇形科植物荆芥的干燥地上部分',
  origin = '河北、江苏、浙江',
  dosage_usage = '煎服',
  dosage_note = '炒炭止血用',
  processing_method = '生用或炒炭。荆芥穗发散力强，荆芥炭止血',
  contraindication = '表虚自汗及阴虚头痛者禁用',
  precaution = '阴虚火旺者慎用',
  incompatibility = '反驴肉、鱼',
  identification = '茎方柱形，表面淡黄绿色或淡紫红色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '荆芥' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0005',
  latin_name = 'Saposhnikovia divaricata (Turcz.) Schischk.',
  source = '伞形科植物防风的干燥根',
  origin = '黑龙江、内蒙古、吉林',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用或炒炭',
  contraindication = '阴血亏虚、热病动风者不宜',
  precaution = '血虚痉急或头痛不因风邪者忌服',
  incompatibility = '恶干姜、藜芦、白蔹、芫花',
  identification = '长圆锥形或长圆柱形，表面灰棕色，有纵皱纹及横长皮孔',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '防风' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0006',
  latin_name = 'Notopterygium incisum Ting ex H.T.Chang',
  source = '伞形科植物羌活的干燥根茎和根',
  origin = '四川、青海、甘肃',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用',
  contraindication = '阴虚头痛者慎用',
  precaution = '血虚痹痛慎用，用量不宜过多，以免耗气伤阴',
  incompatibility = NULL,
  identification = '蚕形，略弯曲，表面棕褐色至黑褐色，外皮脱落处呈黄色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '羌活' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0007',
  latin_name = 'Angelica dahurica (Fisch.) Benth. et Hook.',
  source = '伞形科植物白芷的干燥根',
  origin = '河南、河北、四川',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用',
  contraindication = '阴虚血热者禁用',
  precaution = '阴虚火旺者忌用',
  incompatibility = '反旋覆花',
  identification = '长圆锥形，表面灰棕色或黄棕色，有纵皱纹及横向皮孔',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '白芷' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0008',
  latin_name = 'Asarum heterotropoides Fr.Schmidt',
  source = '马兜铃科植物北细辛的干燥根和根茎',
  origin = '辽宁、吉林、黑龙江',
  dosage_usage = '煎服',
  dosage_note = '用量不宜超过3g，散剂每次服0.5~1g',
  processing_method = '生用',
  contraindication = '气虚多汗、阴虚阳亢头痛、肺热咳喘者禁用',
  precaution = '不宜与藜芦同用。反藜芦',
  incompatibility = '反藜芦',
  identification = '根茎横走，表面灰棕色，根细长，表面灰黄色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '细辛' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0009',
  latin_name = 'Zingiber officinale Rosc.',
  source = '姜科植物姜的新鲜根茎',
  origin = '四川、贵州、广西',
  dosage_usage = '煎服或捣汁冲服',
  dosage_note = NULL,
  processing_method = '生用',
  contraindication = '阴虚内热者忌服',
  precaution = '热性病证不宜用',
  incompatibility = NULL,
  identification = '扁平不规则块状，表面黄褐色或灰棕色，有分枝',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '生姜' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0010',
  latin_name = 'Mosla chinensis Maxim.',
  source = '唇形科植物石香薷的干燥地上部分',
  origin = '江西、河北、河南',
  dosage_usage = '煎服',
  dosage_note = '利水退肿须浓煎',
  processing_method = '生用',
  contraindication = '表虚有汗者忌用',
  precaution = '火盛气虚者禁用',
  incompatibility = NULL,
  identification = '茎方柱形，表面黄绿色或紫褐色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '香薷' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0011',
  latin_name = 'Mentha haplocalyx Briq.',
  source = '唇形科植物薄荷的干燥地上部分',
  origin = '江苏、安徽、江西',
  dosage_usage = '煎服，后下',
  dosage_note = '后下，不宜久煎',
  processing_method = '生用',
  contraindication = '体虚多汗者不宜',
  precaution = '阴虚血燥、肝阳偏亢、表虚汗多者忌服',
  incompatibility = NULL,
  identification = '茎方柱形，表面紫棕色或淡绿色，叶对生皱缩',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '薄荷' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0012',
  latin_name = 'Arctium lappa L.',
  source = '菊科植物牛蒡的干燥成熟果实',
  origin = '吉林、辽宁、黑龙江',
  dosage_usage = '煎服',
  dosage_note = '炒用可减其寒滑之性',
  processing_method = '生用或炒用',
  contraindication = '气虚便溏者慎用',
  precaution = '脾虚便溏者慎用',
  incompatibility = NULL,
  identification = '瘦果长倒卵形，略扁微弯曲，表面灰褐色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '牛蒡子' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0013',
  latin_name = 'Cryptotympana pustulata Fabricius',
  source = '蝉科昆虫黑蚱的若虫羽化时脱落的皮壳',
  origin = '山东、河南、河北',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用',
  contraindication = '孕妇慎用',
  precaution = '脾胃虚弱者慎用',
  incompatibility = NULL,
  identification = '椭圆形而弯曲，全体黄棕色半透明，头部有触角1对',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '蝉蜕' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0014',
  latin_name = 'Morus alba L.',
  source = '桑科植物桑的干燥叶',
  origin = '全国各地均产',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用或蜜炙',
  contraindication = '肝燥者禁用',
  precaution = NULL,
  incompatibility = NULL,
  identification = '多皱缩破碎，完整者卵形或宽卵形，边缘有锯齿',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '桑叶' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0015',
  latin_name = 'Chrysanthemum morifolium Ramat.',
  source = '菊科植物菊的干燥头状花序',
  origin = '浙江（杭菊）、安徽（滁菊、亳菊）、河南（怀菊）',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用',
  contraindication = '气虚胃寒者慎用',
  precaution = '脾胃虚寒者少用',
  incompatibility = NULL,
  identification = '头状花序呈扁球形或不规则球形，花色黄、白、紫等',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '菊花' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0016',
  latin_name = 'Pueraria lobata (Willd.) Ohwi',
  source = '豆科植物野葛的干燥根',
  origin = '湖南、河南、广东',
  dosage_usage = '煎服',
  dosage_note = '退热生津宜煨用',
  processing_method = '生用或煨用',
  contraindication = '夏季表虚汗多者忌用',
  precaution = '胃寒呕吐者慎用',
  incompatibility = NULL,
  identification = '纵切片呈长方形，横切片呈半圆形，表面黄白色或淡棕色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '葛根' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0017',
  latin_name = 'Bupleurum chinense DC.',
  source = '伞形科植物柴胡的干燥根',
  origin = '河北、河南、辽宁',
  dosage_usage = '煎服',
  dosage_note = '解表退热用量宜稍大，疏肝解郁宜醋炙',
  processing_method = '生用、醋炙或酒炙',
  contraindication = '真阴亏损、肝阳上升者忌服',
  precaution = '阴虚阳亢、肝风内动者忌用，大叶柴胡有毒勿用',
  incompatibility = NULL,
  identification = '圆锥形或圆柱形，根头膨大，表面黑褐色或浅棕色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '柴胡' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0018',
  latin_name = 'Cimicifuga heracleifolia Kom.',
  source = '毛茛科植物大三叶升麻的干燥根茎',
  origin = '辽宁、黑龙江、四川',
  dosage_usage = '煎服',
  dosage_note = '发表透疹宜生用，升阳举陷宜炙用',
  processing_method = '生用或蜜炙',
  contraindication = '麻疹已透及阴虚火旺者禁用',
  precaution = '阴虚阳浮者忌用',
  incompatibility = NULL,
  identification = '不规则长形块状，多分枝，表面黑褐色或棕褐色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '升麻' AND deleted = 0;

-- ==================== 清热药 ====================

UPDATE herb_knowledge SET
  herb_code = 'HK0019',
  latin_name = 'Gypsum Fibrosum (CaSO4·2H2O)',
  source = '硫酸盐类矿物硬石膏族石膏',
  origin = '湖北、安徽、山东',
  dosage_usage = '煎服，先煎',
  dosage_note = '先煎15~20分钟，用量宜大',
  processing_method = '生用或煅用。清热泻火宜生用，敛疮生肌宜煅用',
  contraindication = '脾胃虚寒及阴虚内热者禁用',
  precaution = '脾胃虚弱、血虚发热者慎用',
  incompatibility = NULL,
  identification = '纤维状集合体，白色、灰白色或淡黄色，纵断面有绢丝样光泽',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '石膏' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0020',
  latin_name = 'Anemarrhena asphodeloides Bge.',
  source = '百合科植物知母的干燥根茎',
  origin = '河北、山西、内蒙古',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用或盐水炙。清热泻火宜生用，滋阴降火宜盐炙',
  contraindication = '脾胃虚寒、大便溏泄者禁用',
  precaution = '脾虚便溏者不宜用',
  incompatibility = NULL,
  identification = '长条状，微弯曲，表面黄棕色至棕色，一面有纵沟',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '知母' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0021',
  latin_name = 'Gardenia jasminoides Ellis',
  source = '茜草科植物栀子的干燥成熟果实',
  origin = '江西、湖南、浙江',
  dosage_usage = '煎服',
  dosage_note = '炒炭止血用',
  processing_method = '生用、炒焦或炒炭',
  contraindication = '脾虚便溏者不宜',
  precaution = '脾虚便溏者慎用',
  incompatibility = NULL,
  identification = '长卵圆形或椭圆形，表面红黄色或棕红色，具6条翅状纵棱',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '栀子' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0022',
  latin_name = 'Prunella vulgaris L.',
  source = '唇形科植物夏枯草的干燥果穗',
  origin = '江苏、安徽、浙江',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用',
  contraindication = '脾胃虚弱者慎用',
  precaution = NULL,
  incompatibility = NULL,
  identification = '棒状，略扁，表面棕红色或棕褐色，由数至十余轮宿萼组成',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '夏枯草' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0023',
  latin_name = 'Phragmites communis Trin.',
  source = '禾本科植物芦苇的新鲜或干燥根茎',
  origin = '全国各地均产',
  dosage_usage = '煎服。鲜品用量加倍',
  dosage_note = '鲜品效果更佳，用量30~60g',
  processing_method = '生用。鲜用或干用',
  contraindication = '脾胃虚寒者慎用',
  precaution = NULL,
  incompatibility = NULL,
  identification = '长圆柱形，有节，表面黄白色，有光泽',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '芦根' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0024',
  latin_name = 'Scutellaria baicalensis Georgi',
  source = '唇形科植物黄芩的干燥根',
  origin = '河北、山西、内蒙古',
  dosage_usage = '煎服',
  dosage_note = '清热宜生用，安胎宜炒用',
  processing_method = '生用、酒炙、炒炭',
  contraindication = '脾肺虚热者禁用',
  precaution = '脾胃虚寒者不宜使用',
  incompatibility = '恶葱实，畏丹砂、牡丹、藜芦',
  identification = '圆锥形，扭曲，表面棕黄色或深黄色，老根中心枯朽呈空洞',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '黄芩' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0025',
  latin_name = 'Coptis chinensis Franch.',
  source = '毛茛科植物黄连的干燥根茎',
  origin = '四川（味连）、湖北、云南（云连）',
  dosage_usage = '煎服',
  dosage_note = '用量不宜过大，脾胃虚寒者忌',
  processing_method = '生用、姜汁炙或酒炙',
  contraindication = '脾胃虚寒者禁用，苦燥伤阴不宜久服',
  precaution = '胃虚呕恶、脾虚泄泻者均禁用',
  incompatibility = '恶菊花、芫花、玄参、白鲜皮；畏款冬',
  identification = '多分枝，集聚成簇状，形如鸡爪，表面灰黄色或黄褐色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '黄连' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0026',
  latin_name = 'Phellodendron chinense Schneid.',
  source = '芸香科植物黄皮树的干燥树皮',
  origin = '四川、贵州、湖北',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用、盐水炙或炒炭',
  contraindication = '脾胃虚寒者忌用',
  precaution = '苦寒伤胃，不宜久服',
  incompatibility = NULL,
  identification = '板片状或浅槽状，外表面黄褐色或黄棕色，内表面暗黄色或淡棕色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '黄柏' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0027',
  latin_name = 'Gentiana manshurica Kitag.',
  source = '龙胆科植物龙胆的干燥根和根茎',
  origin = '黑龙江、辽宁、吉林',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用',
  contraindication = '脾胃虚弱者慎用，勿多服久服',
  precaution = '阴虚津伤者慎用',
  incompatibility = NULL,
  identification = '根茎呈不规则块状，根数条，细长圆柱形，表面淡黄色或黄棕色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '龙胆' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0028',
  latin_name = 'Sophora flavescens Ait.',
  source = '豆科植物苦参的干燥根',
  origin = '山西、河北、河南',
  dosage_usage = '煎服。外用适量',
  dosage_note = NULL,
  processing_method = '生用',
  contraindication = '脾胃虚寒者忌用',
  precaution = '反藜芦',
  incompatibility = '反藜芦',
  identification = '长圆柱形，下部常分枝，表面灰棕色或棕黄色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '苦参' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0029',
  latin_name = 'Lonicera japonica Thunb.',
  source = '忍冬科植物忍冬的干燥花蕾或带初开的花',
  origin = '山东、河南、河北',
  dosage_usage = '煎服',
  dosage_note = '外感风热多生用，炒炭止痢',
  processing_method = '生用或炒炭',
  contraindication = '脾胃虚寒及气虚疮疡脓清者忌用',
  precaution = '脾胃虚寒者不宜用',
  incompatibility = NULL,
  identification = '棒状，上粗下细，表面黄白色或绿白色，密被短柔毛',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '金银花' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0030',
  latin_name = 'Forsythia suspensa (Thunb.) Vahl',
  source = '木犀科植物连翘的干燥果实',
  origin = '山西、陕西、河南',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用',
  contraindication = '脾胃虚弱、气虚发热者不宜',
  precaution = '脾胃虚弱者慎用',
  incompatibility = NULL,
  identification = '长卵形至卵形，表面有不规则的纵皱纹和多数凸起的小斑点',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '连翘' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0031',
  latin_name = 'Taraxacum mongolicum Hand.-Mazz.',
  source = '菊科植物蒲公英的干燥全草',
  origin = '全国各地均产',
  dosage_usage = '煎服。外用鲜品捣敷',
  dosage_note = '鲜品效佳',
  processing_method = '生用',
  contraindication = '阳虚外寒、脾胃虚弱者忌用',
  precaution = '用量过大可致缓泻',
  incompatibility = NULL,
  identification = '皱缩卷曲的团块，根圆锥形，表面棕褐色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '蒲公英' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0032',
  latin_name = 'Isatis indigotica Fort.',
  source = '十字花科植物菘蓝的干燥根',
  origin = '河北、安徽、江苏',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用',
  contraindication = '体虚而无实火热毒者忌服',
  precaution = '脾胃虚寒者慎用',
  incompatibility = NULL,
  identification = '圆柱形，稍弯曲，表面淡灰黄色或淡棕黄色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '板蓝根' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0033',
  latin_name = 'Houttuynia cordata Thunb.',
  source = '三白草科植物蕺菜的干燥地上部分',
  origin = '浙江、江苏、湖北',
  dosage_usage = '煎服，不宜久煎。鲜品用量加倍',
  dosage_note = '不宜久煎，后下',
  processing_method = '生用。鲜用效佳',
  contraindication = '虚寒证及阴性疮疡忌服',
  precaution = '虚寒症不宜用',
  incompatibility = NULL,
  identification = '茎扁圆柱形，扭曲，表面棕黄色，有纵棱',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '鱼腥草' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0034',
  latin_name = 'Hedyotis diffusa Willd.',
  source = '茜草科植物白花蛇舌草的干燥全草',
  origin = '福建、广东、广西',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用',
  contraindication = '阴疽及脾胃虚寒者忌用',
  precaution = NULL,
  incompatibility = NULL,
  identification = '全草缠绕成团，灰绿色或灰棕色',
  is_precious = 0,
  medical_insurance = '乙'
WHERE herb_name = '白花蛇舌草' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0035',
  latin_name = 'Rehmannia glutinosa Libosch.',
  source = '玄参科植物地黄的新鲜或干燥块根',
  origin = '河南（怀庆地黄）',
  dosage_usage = '煎服。鲜品用量加倍，可捣汁入药',
  dosage_note = '鲜生地清热凉血力强',
  processing_method = '鲜用或干用',
  contraindication = '脾虚湿滞、腹满便溏者不宜',
  precaution = '脾虚泄泻、胃虚食少者慎用',
  incompatibility = NULL,
  identification = '不规则团块状或长圆形，表面棕黑色或棕灰色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '生地黄' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0036',
  latin_name = 'Scrophularia ningpoensis Hemsl.',
  source = '玄参科植物玄参的干燥根',
  origin = '浙江、湖北、四川',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用',
  contraindication = '脾胃虚寒、食少便溏者不宜',
  precaution = '不宜与藜芦同用。反藜芦',
  incompatibility = '反藜芦',
  identification = '类圆柱形，中间略粗或上粗下细，表面灰黄色或灰褐色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '玄参' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0037',
  latin_name = 'Paeonia suffruticosa Andr.',
  source = '毛茛科植物牡丹的干燥根皮',
  origin = '安徽（铜陵、亳州）、山东、河南',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用或炒炭。清热凉血宜生用，止血宜炒炭',
  contraindication = '血虚有寒、孕妇及月经过多者慎用',
  precaution = '孕妇不宜用',
  incompatibility = '畏菟丝子、大黄、贝母',
  identification = '筒状或半筒状，外表面灰褐色或黄褐色，有横长皮孔',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '牡丹皮' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0038',
  latin_name = 'Paeonia lactiflora Pall.',
  source = '毛茛科植物芍药的干燥根',
  origin = '内蒙古、四川、安徽',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用',
  contraindication = '血虚无瘀滞者不宜单用',
  precaution = '虚寒证不宜用',
  incompatibility = '反藜芦',
  identification = '圆柱形，稍弯曲，表面棕褐色，粗糙',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '赤芍' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0039',
  latin_name = 'Artemisia annua L.',
  source = '菊科植物黄花蒿的干燥地上部分',
  origin = '全国各地均产',
  dosage_usage = '煎服，后下',
  dosage_note = '后下，不宜久煎',
  processing_method = '生用。鲜用更佳',
  contraindication = '产后血虚、内寒作泻者慎用',
  precaution = '脾胃虚弱者慎用',
  incompatibility = NULL,
  identification = '茎圆柱形，上部多分枝，表面黄绿色或棕黄色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '青蒿' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0040',
  latin_name = 'Lycium chinense Mill.',
  source = '茄科植物枸杞的干燥根皮',
  origin = '山西、河南、浙江',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用',
  contraindication = '外感风寒发热者忌用',
  precaution = '脾胃虚寒者慎用',
  incompatibility = NULL,
  identification = '筒状或槽状，外表面灰黄色至棕黄色，内表面黄白色至灰黄色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '地骨皮' AND deleted = 0;

-- ==================== 泻下药 ====================

UPDATE herb_knowledge SET
  herb_code = 'HK0041',
  latin_name = 'Rheum palmatum L.',
  source = '蓼科植物掌叶大黄的干燥根和根茎',
  origin = '青海、甘肃、四川',
  dosage_usage = '煎服，后下或开水泡服。外用适量',
  dosage_note = '后下，不宜久煎。生用泻下力强，酒制活血，炒炭止血',
  processing_method = '生用、酒炙、醋炙或炒炭',
  contraindication = '脾胃虚弱者慎用，妇女怀孕、月经期、哺乳期忌用',
  precaution = '孕妇禁用，哺乳期妇女慎用',
  incompatibility = NULL,
  identification = '类圆柱形、圆锥形，表面黄棕色至红棕色，可见类白色网状纹理',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '大黄' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0042',
  latin_name = 'Natrii Sulfas (Na2SO4·10H2O)',
  source = '硫酸盐类矿物芒硝经加工精制而成的结晶体',
  origin = '河北、山东、河南',
  dosage_usage = '冲入药汁内或开水溶化后服',
  dosage_note = '不入煎剂，溶服',
  processing_method = '生用或风化',
  contraindication = '孕妇禁用，脾胃虚寒者忌服',
  precaution = '孕妇、哺乳期妇女禁用',
  incompatibility = '忌三棱',
  identification = '棱柱状、长方形或不规则结晶，无色透明',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '芒硝' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0043',
  latin_name = 'Cannabis sativa L.',
  source = '桑科植物大麻的干燥成熟种仁',
  origin = '黑龙江、辽宁、吉林',
  dosage_usage = '煎服，打碎入煎',
  dosage_note = '打碎入煎',
  processing_method = '生用，去壳取仁',
  contraindication = '脾虚便溏或肠滑者禁用',
  precaution = '大便滑泻者慎用',
  incompatibility = '畏牡蛎、白薇，恶茯苓',
  identification = '卵圆形，表面灰绿色或灰黄色，有微细网状纹',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '火麻仁' AND deleted = 0;

-- ==================== 祛风湿药 ====================

UPDATE herb_knowledge SET
  herb_code = 'HK0044',
  latin_name = 'Angelica pubescens Maxim.',
  source = '伞形科植物重齿毛当归的干燥根',
  origin = '四川、湖北、安徽',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用',
  contraindication = '阴虚血燥者慎用',
  precaution = '阴虚血燥者不宜使用',
  incompatibility = NULL,
  identification = '根略呈圆柱形，下部2~3分枝，表面灰褐色或棕褐色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '独活' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0045',
  latin_name = 'Clematis chinensis Osbeck',
  source = '毛茛科植物威灵仙的干燥根和根茎',
  origin = '江苏、安徽、浙江',
  dosage_usage = '煎服',
  dosage_note = '治骨鲠可用醋煎服',
  processing_method = '生用',
  contraindication = '气血虚弱者慎用',
  precaution = '气虚血弱，无风寒湿邪者忌服',
  incompatibility = NULL,
  identification = '根茎呈柱状，表面淡棕黄色，根呈细长圆柱形',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '威灵仙' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0046',
  latin_name = 'Gentiana macrophylla Pall.',
  source = '龙胆科植物秦艽的干燥根',
  origin = '甘肃、陕西、内蒙古',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用',
  contraindication = '久痛虚羸、溲多便滑者忌服',
  precaution = '脾胃虚寒者慎用',
  incompatibility = '畏牛乳',
  identification = '类圆锥形，上粗下细，扭曲不直，表面灰黄色或棕黄色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '秦艽' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0047',
  latin_name = 'Taxillus chinensis (DC.) Danser',
  source = '桑寄生科植物桑寄生的干燥带叶茎枝',
  origin = '广东、广西、云南',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用',
  contraindication = NULL,
  precaution = NULL,
  incompatibility = NULL,
  identification = '茎枝呈圆柱形，表面红褐色或灰褐色，叶多卷曲',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '桑寄生' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0048',
  latin_name = 'Acanthopanax gracilistylus W.W.Smith',
  source = '五加科植物细柱五加的干燥根皮',
  origin = '湖北、河南、安徽',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用',
  contraindication = '阴虚火旺者慎用',
  precaution = '南五加皮无毒可用，北五加皮（香加皮）有毒勿混',
  incompatibility = NULL,
  identification = '长筒状或不规则片状，外表面灰褐色，内表面淡黄色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '五加皮' AND deleted = 0;

-- ==================== 化湿药 ====================

UPDATE herb_knowledge SET
  herb_code = 'HK0049',
  latin_name = 'Pogostemon cablin (Blanco) Benth.',
  source = '唇形科植物广藿香的干燥地上部分',
  origin = '广东（石牌藿香）、海南',
  dosage_usage = '煎服，后下',
  dosage_note = '后下，不宜久煎',
  processing_method = '生用',
  contraindication = '阴虚火旺者禁用',
  precaution = '阴虚者不宜用',
  incompatibility = NULL,
  identification = '茎略呈方柱形，表面被柔毛，叶对生，皱缩成团',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '藿香' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0050',
  latin_name = 'Eupatorium fortunei Turcz.',
  source = '菊科植物佩兰的干燥地上部分',
  origin = '江苏、浙江、河北',
  dosage_usage = '煎服。鲜品用量加倍',
  dosage_note = '后下，不宜久煎',
  processing_method = '生用或鲜用',
  contraindication = '阴虚血燥者慎用',
  precaution = '胃气虚者慎用',
  incompatibility = NULL,
  identification = '茎圆柱形，表面黄棕色或黄绿色，叶对生，多皱缩',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '佩兰' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0051',
  latin_name = 'Atractylodes lancea (Thunb.) DC.',
  source = '菊科植物茅苍术的干燥根茎',
  origin = '江苏（茅山）、湖北、河南',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用或麸炒',
  contraindication = '阴虚内热、气虚多汗者忌用',
  precaution = '阴虚燥渴者不宜用',
  incompatibility = NULL,
  identification = '不规则连珠状或结节状圆柱形，表面灰棕色，断面散有橙黄色油室',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '苍术' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0052',
  latin_name = 'Magnolia officinalis Rehd. et Wils.',
  source = '木兰科植物厚朴的干燥干皮、根皮及枝皮',
  origin = '四川、湖北、浙江',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用或姜汁炙',
  contraindication = '孕妇慎用',
  precaution = '气虚津亏者及孕妇慎用',
  incompatibility = '忌豆，畏硝石',
  identification = '卷筒状或双卷筒状，外表面灰棕色或灰褐色，内表面紫棕色或深紫褐色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '厚朴' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0053',
  latin_name = 'Amomum villosum Lour.',
  source = '姜科植物阳春砂的干燥成熟果实',
  origin = '广东（阳春）、广西、云南',
  dosage_usage = '煎服，后下',
  dosage_note = '后下，不宜久煎',
  processing_method = '生用',
  contraindication = '阴虚有热者忌服',
  precaution = '阴虚血燥者忌用',
  incompatibility = NULL,
  identification = '椭圆形或卵圆形，表面棕褐色，密生刺状突起',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '砂仁' AND deleted = 0;

-- ==================== 利水渗湿药 ====================

UPDATE herb_knowledge SET
  herb_code = 'HK0054',
  latin_name = 'Poria cocos (Schw.) Wolf',
  source = '多孔菌科真菌茯苓的干燥菌核',
  origin = '云南、安徽、湖北',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用',
  contraindication = '虚寒精滑或气虚下陷者忌服',
  precaution = '阴虚而无湿热者不宜',
  incompatibility = '畏牡蒙、地榆、雄黄、秦艽、龟甲；忌米醋',
  identification = '外皮薄而粗糙，棕褐色至黑褐色，断面颗粒性，白色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '茯苓' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0055',
  latin_name = 'Alisma orientale (Sam.) Juzep.',
  source = '泽泻科植物泽泻的干燥块茎',
  origin = '福建（建泽泻）、四川（川泽泻）',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用或盐水炙',
  contraindication = '肾虚精滑者禁用',
  precaution = '肾虚精滑无湿热者忌服',
  incompatibility = '畏海蛤、文蛤',
  identification = '类球形、椭圆形或卵圆形，表面黄白色或淡黄棕色，有不规则横向环状浅沟纹',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '泽泻' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0056',
  latin_name = 'Coix lacryma-jobi L. var. ma-yuen (Roman.) Stapf',
  source = '禾本科植物薏苡的干燥成熟种仁',
  origin = '福建、河北、辽宁',
  dosage_usage = '煎服',
  dosage_note = '清热利湿宜生用，健脾止泻宜炒用',
  processing_method = '生用或麸炒',
  contraindication = '孕妇慎用',
  precaution = '津液不足者慎用，孕妇慎用',
  incompatibility = NULL,
  identification = '宽卵形或长椭圆形，表面乳白色，偶有残存淡棕色种皮',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '薏苡仁' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0057',
  latin_name = 'Polyporus umbellatus (Pers.) Fries',
  source = '多孔菌科真菌猪苓的干燥菌核',
  origin = '陕西、云南、四川',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用',
  contraindication = '无水湿者不宜用',
  precaution = '无水湿者忌服',
  incompatibility = NULL,
  identification = '不规则块状或条状，表面黑色、灰黑色或棕黑色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '猪苓' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0058',
  latin_name = 'Plantago asiatica L.',
  source = '车前科植物车前的干燥成熟种子',
  origin = '江西、河南、黑龙江',
  dosage_usage = '煎服，包煎',
  dosage_note = '包煎',
  processing_method = '生用或盐水炙',
  contraindication = '肾虚精滑者慎用',
  precaution = '内伤劳倦者忌服',
  incompatibility = NULL,
  identification = '椭圆形或不规则长圆形，表面黑褐色或棕褐色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '车前子' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0059',
  latin_name = 'Clematis armandii Franch.',
  source = '毛茛科植物小木通的干燥藤茎',
  origin = '四川、贵州、湖北',
  dosage_usage = '煎服',
  dosage_note = '不宜过量或久服',
  processing_method = '生用',
  contraindication = '孕妇及内无湿热者慎用',
  precaution = '关木通含马兜铃酸有肾毒性，现用川木通代替',
  incompatibility = NULL,
  identification = '圆柱形，表面黄棕色或黄褐色，具纵向凹沟',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '木通' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0060',
  latin_name = 'Lysimachia christinae Hance',
  source = '报春花科植物过路黄的干燥全草',
  origin = '四川、江苏、浙江',
  dosage_usage = '煎服。鲜品用量加倍',
  dosage_note = '鲜品效果更佳',
  processing_method = '生用',
  contraindication = '脾虚无湿热者慎用',
  precaution = NULL,
  incompatibility = NULL,
  identification = '全草缠绕成团，叶对生，圆形或宽卵形',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '金钱草' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0061',
  latin_name = 'Artemisia capillaris Thunb.',
  source = '菊科植物滨蒿或茵陈蒿的干燥地上部分',
  origin = '陕西、山西、安徽',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用',
  contraindication = '蓄血发黄者及血虚萎黄者慎用',
  precaution = '非湿热所致之黄疸不宜用',
  incompatibility = NULL,
  identification = '幼嫩茎叶卷曲成团状，灰白色或灰绿色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '茵陈' AND deleted = 0;

-- ==================== 温里药 ====================

UPDATE herb_knowledge SET
  herb_code = 'HK0062',
  latin_name = 'Aconitum carmichaelii Debx.',
  source = '毛茛科植物乌头的子根的加工品',
  origin = '四川（江油附子）',
  dosage_usage = '煎服，先煎0.5~1小时',
  dosage_note = '必须先煎30~60分钟以减毒，煎至口尝无麻辣感为度',
  processing_method = '盐附子、黑顺片、白附片等炮制品',
  contraindication = '孕妇禁用。阴虚阳亢者忌用',
  precaution = '不宜与半夏、瓜蒌、贝母、白蔹、白及同用。反半夏、瓜蒌、贝母、白蔹、白及',
  incompatibility = '反半夏、瓜蒌、贝母、白蔹、白及（十八反）',
  identification = '圆锥形，表面灰黑色（盐附子），表面暗黄色（黑顺片）',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '附子' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0063',
  latin_name = 'Zingiber officinale Rosc.',
  source = '姜科植物姜的干燥根茎',
  origin = '四川、贵州、广西',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用或炮用。干姜温中力强，炮姜偏于温经止血',
  contraindication = '阴虚内热者忌用，孕妇慎用',
  precaution = '热性出血者忌用',
  incompatibility = NULL,
  identification = '扁平块状，表面灰黄色或浅灰棕色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '干姜' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0064',
  latin_name = 'Cinnamomum cassia Presl',
  source = '樟科植物肉桂的干燥树皮',
  origin = '广西、广东、云南',
  dosage_usage = '煎服，后下或焗服',
  dosage_note = '后下或研末冲服，每次1~2g',
  processing_method = '生用',
  contraindication = '阴虚火旺、里有实热、血热妄行及孕妇禁用',
  precaution = '畏赤石脂。不宜与赤石脂同用',
  incompatibility = '畏赤石脂',
  identification = '槽状或卷筒状，外表面灰棕色，稍粗糙，内表面红棕色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '肉桂' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0065',
  latin_name = 'Evodia rutaecarpa (Juss.) Benth.',
  source = '芸香科植物吴茱萸的干燥近成熟果实',
  origin = '贵州、广西、湖南',
  dosage_usage = '煎服',
  dosage_note = '用量不宜过大，以免中毒',
  processing_method = '生用或制用（甘草水制）',
  contraindication = '阴虚火旺者忌服',
  precaution = '有小毒，不宜多服久服',
  incompatibility = NULL,
  identification = '球形或略扁球形，表面暗黄绿色至褐色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '吴茱萸' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0066',
  latin_name = 'Zanthoxylum bungeanum Maxim.',
  source = '芸香科植物花椒的干燥成熟果皮',
  origin = '四川（汉源）、陕西、甘肃',
  dosage_usage = '煎服。外用适量',
  dosage_note = NULL,
  processing_method = '生用或炒用',
  contraindication = '阴虚火旺者忌服，孕妇慎用',
  precaution = NULL,
  incompatibility = NULL,
  identification = '球形，沿腹缝线开裂，表面紫红色或棕红色，散生油点',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '花椒' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0067',
  latin_name = 'Foeniculum vulgare Mill.',
  source = '伞形科植物茴香的干燥成熟果实',
  origin = '内蒙古、山西、甘肃',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用或盐水炙',
  contraindication = '阴虚火旺者禁用',
  precaution = '阴虚者慎用',
  incompatibility = NULL,
  identification = '双悬果，呈圆柱形，表面黄绿色至棕黄色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '小茴香' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0068',
  latin_name = 'Eugenia caryophyllata Thunb.',
  source = '桃金娘科植物丁香的干燥花蕾',
  origin = '坦桑尼亚、马来西亚、印度尼西亚（进口）',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用',
  contraindication = '热病及阴虚内热者忌用',
  precaution = '畏郁金。不宜与郁金同用',
  incompatibility = '畏郁金',
  identification = '略呈研棒状，花冠圆球形，花萼管圆柱状，表面粗糙',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '丁香' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0069',
  latin_name = 'Alpinia officinarum Hance',
  source = '姜科植物高良姜的干燥根茎',
  origin = '广东（徐闻）、广西、海南',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用',
  contraindication = '阴虚有热者忌服',
  precaution = NULL,
  incompatibility = NULL,
  identification = '圆柱形，多弯曲，有分枝，表面棕红色至暗褐色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '高良姜' AND deleted = 0;

-- ==================== 理气药 ====================

UPDATE herb_knowledge SET
  herb_code = 'HK0070',
  latin_name = 'Citrus reticulata Blanco',
  source = '芸香科植物橘的干燥成熟果皮',
  origin = '广东（新会陈皮）、福建、四川',
  dosage_usage = '煎服',
  dosage_note = '陈久者良，故名陈皮',
  processing_method = '生用',
  contraindication = '气虚体燥、阴虚燥咳、吐血者慎用',
  precaution = '温燥，阴虚燥咳者不宜多用',
  incompatibility = NULL,
  identification = '常剥成数瓣，基部相连，外表面橙红色或红棕色，有细皱纹及小凹点',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '陈皮' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0071',
  latin_name = 'Citrus aurantium L.',
  source = '芸香科植物酸橙的干燥幼果',
  origin = '四川（江津）、江西、湖南',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用或麸炒',
  contraindication = '脾胃虚弱及孕妇慎用',
  precaution = '孕妇慎用',
  incompatibility = NULL,
  identification = '半球形，外表面黑绿色或暗棕绿色，具颗粒状突起',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '枳实' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0072',
  latin_name = 'Aucklandia lappa Decne.',
  source = '菊科植物木香的干燥根',
  origin = '云南（丽江）、四川',
  dosage_usage = '煎服',
  dosage_note = '生用行气力强，煨用止泻',
  processing_method = '生用或煨用',
  contraindication = '阴虚津液不足者慎用',
  precaution = '气阴不足者不宜用',
  incompatibility = NULL,
  identification = '圆柱形或半圆柱形，表面黄棕色至灰褐色，有明显纵沟纹及侧根痕',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '木香' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0073',
  latin_name = 'Cyperus rotundus L.',
  source = '莎草科植物莎草的干燥根茎',
  origin = '广东、河南、四川',
  dosage_usage = '煎服',
  dosage_note = '生用上行胸膈，熟用下走肝肾',
  processing_method = '生用、醋炙或酒炙',
  contraindication = '气虚无滞者慎用',
  precaution = '阴虚血热者忌用',
  incompatibility = NULL,
  identification = '纺锤形，表面棕褐色或黑褐色，有纵皱纹',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '香附' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0074',
  latin_name = 'Citrus medica L. var. sarcodactylis Swingle',
  source = '芸香科植物佛手的干燥果实',
  origin = '广东、福建、四川',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用',
  contraindication = '阴虚体弱者慎用',
  precaution = '阴虚有火、无气滞症状者慎用',
  incompatibility = NULL,
  identification = '长条形或片状，表面黄绿色或橙黄色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '佛手' AND deleted = 0;

-- ==================== 消食药 ====================

UPDATE herb_knowledge SET
  herb_code = 'HK0075',
  latin_name = 'Crataegus pinnatifida Bge.',
  source = '蔷薇科植物山里红的干燥成熟果实',
  origin = '山东、河北、河南',
  dosage_usage = '煎服',
  dosage_note = '生用消食散瘀，炒用醒脾止泻，炒炭止血止泻',
  processing_method = '生用、炒用或炒炭',
  contraindication = '脾胃虚弱而无积滞者不宜',
  precaution = '胃酸过多者慎用',
  incompatibility = NULL,
  identification = '圆形片状，外皮红色，具皱纹，有灰白色小斑点',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '山楂' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0076',
  latin_name = 'Massa Medicata Fermentata',
  source = '辣蓼、青蒿、杏仁等药加入面粉或麸皮混合后经发酵制成的加工品',
  origin = '全国各地均产',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用或炒焦',
  contraindication = NULL,
  precaution = NULL,
  incompatibility = NULL,
  identification = '方形或长方形块状，表面灰黄色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '神曲' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0077',
  latin_name = 'Hordeum vulgare L.',
  source = '禾本科植物大麦的成熟果实经发芽干燥的炮制品',
  origin = '全国各地均产',
  dosage_usage = '煎服',
  dosage_note = '回乳用量宜大(60~120g)',
  processing_method = '生用或炒用',
  contraindication = '哺乳期妇女不宜用（有回乳作用）',
  precaution = '授乳期妇女不宜使用',
  incompatibility = NULL,
  identification = '梭形，表面淡黄色，有须根残痕',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '麦芽' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0078',
  latin_name = 'Raphanus sativus L.',
  source = '十字花科植物萝卜的干燥成熟种子',
  origin = '全国各地均产',
  dosage_usage = '煎服',
  dosage_note = '生用涌吐风痰，炒用降气化痰消食',
  processing_method = '生用或炒用',
  contraindication = '气虚无食积痰滞者慎用',
  precaution = '不宜与人参同用',
  incompatibility = NULL,
  identification = '类卵圆形或椭圆形，稍扁，表面黄棕色或红棕色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '莱菔子' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0079',
  latin_name = 'Gallus gallus domesticus Brisson',
  source = '雉科动物家鸡的干燥沙囊内壁',
  origin = '全国各地均产',
  dosage_usage = '煎服',
  dosage_note = '研末冲服效佳，每次1.5~3g',
  processing_method = '生用、砂炒或醋炙',
  contraindication = '脾虚无积者慎用',
  precaution = NULL,
  incompatibility = NULL,
  identification = '不规则卷片状，黄色、黄绿色或黄褐色，薄而半透明',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '鸡内金' AND deleted = 0;

-- ==================== 止血药 ====================

UPDATE herb_knowledge SET
  herb_code = 'HK0080',
  latin_name = 'Panax notoginseng (Burk.) F.H.Chen',
  source = '五加科植物三七的干燥根和根茎',
  origin = '云南（文山）、广西',
  dosage_usage = '煎服。研末吞服每次1~3g',
  dosage_note = '研末吞服效佳，止血常用粉剂',
  processing_method = '生用或熟用。生三七止血化瘀，熟三七补血',
  contraindication = '孕妇慎用',
  precaution = '孕妇禁用',
  incompatibility = NULL,
  identification = '主根呈类圆锥形或圆柱形，表面灰褐色或灰黄色，有断续纵皱纹',
  is_precious = 1,
  medical_insurance = '甲'
WHERE herb_name = '三七' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0081',
  latin_name = 'Bletilla striata (Thunb.) Reichb.f.',
  source = '兰科植物白及的干燥块茎',
  origin = '贵州、四川、湖南',
  dosage_usage = '煎服。研末吞服每次1.5~3g',
  dosage_note = '研末吞服止血效佳',
  processing_method = '生用',
  contraindication = '外感咯血及肺痈初起者忌用',
  precaution = '不宜与乌头类药材同用。反乌头',
  incompatibility = '反乌头（十八反）',
  identification = '不规则扁圆形，表面灰白色或黄白色，有2~3个环节状突起',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '白及' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0082',
  latin_name = 'Typha angustifolia L.',
  source = '香蒲科植物水烛香蒲的干燥花粉',
  origin = '江苏、浙江、安徽',
  dosage_usage = '煎服，包煎',
  dosage_note = '包煎。生用活血，炒炭止血',
  processing_method = '生用或炒炭',
  contraindication = '孕妇慎用',
  precaution = '孕妇忌服',
  incompatibility = NULL,
  identification = '黄色粉末，体轻，放水中则浮于水面',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '蒲黄' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0083',
  latin_name = 'Rubia cordifolia L.',
  source = '茜草科植物茜草的干燥根和根茎',
  origin = '安徽、河北、陕西',
  dosage_usage = '煎服',
  dosage_note = '止血宜炒炭，活血通经宜生用',
  processing_method = '生用或炒炭',
  contraindication = '脾胃虚寒者慎用',
  precaution = '孕妇慎用',
  incompatibility = '畏鼠妇',
  identification = '根茎呈结节状，根呈圆柱形，表面红棕色或暗棕色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '茜草' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0084',
  latin_name = 'Artemisia argyi Levl. et Vant.',
  source = '菊科植物艾的干燥叶',
  origin = '湖北（蕲春蕲艾）、河南、安徽',
  dosage_usage = '煎服。外用适量，供灸治或熏洗用',
  dosage_note = '炒炭止血',
  processing_method = '生用或炒炭',
  contraindication = '阴虚血热者慎用',
  precaution = '血热者不宜用',
  incompatibility = NULL,
  identification = '多皱缩，破碎，有短柄，上表面灰绿色，下表面密生灰白色绒毛',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '艾叶' AND deleted = 0;

-- ==================== 活血祛瘀药 ====================

UPDATE herb_knowledge SET
  herb_code = 'HK0085',
  latin_name = 'Ligusticum chuanxiong Hort.',
  source = '伞形科植物川芎的干燥根茎',
  origin = '四川（都江堰）',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用或酒炙',
  contraindication = '阴虚火旺、月经过多及孕妇不宜',
  precaution = '阴虚头痛慎用，有上行之性',
  incompatibility = '畏黄连、硝石、山茱萸、狼毒；恶黄芪',
  identification = '不规则结节状拳形团块，表面黄褐色，有多数平行隆起的轮节',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '川芎' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0086',
  latin_name = 'Salvia miltiorrhiza Bge.',
  source = '唇形科植物丹参的干燥根和根茎',
  origin = '四川（中江）、安徽、山东',
  dosage_usage = '煎服',
  dosage_note = '活血化瘀宜酒炙',
  processing_method = '生用或酒炙',
  contraindication = '无瘀血者慎用，孕妇慎用',
  precaution = '反藜芦。月经过多者慎用',
  incompatibility = '反藜芦',
  identification = '根茎短粗，根数条，长圆柱形，表面棕红色或暗棕红色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '丹参' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0087',
  latin_name = 'Carthamus tinctorius L.',
  source = '菊科植物红花的干燥花',
  origin = '新疆、河南、四川',
  dosage_usage = '煎服',
  dosage_note = '少用养血，多用活血',
  processing_method = '生用',
  contraindication = '孕妇忌用，月经过多者慎用',
  precaution = '有出血倾向者不宜',
  incompatibility = NULL,
  identification = '花多聚集成团，红黄色或红色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '红花' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0088',
  latin_name = 'Prunus persica (L.) Batsch',
  source = '蔷薇科植物桃的干燥成熟种子',
  origin = '四川、云南、陕西',
  dosage_usage = '煎服，捣碎入煎',
  dosage_note = '捣碎入煎',
  processing_method = '生用',
  contraindication = '孕妇禁用，便溏者慎用',
  precaution = '有小毒，不宜过量',
  incompatibility = NULL,
  identification = '扁长卵形，表面黄棕色至红棕色，有颗粒状突起',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '桃仁' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0089',
  latin_name = 'Achyranthes bidentata Bl.',
  source = '苋科植物牛膝的干燥根',
  origin = '河南（怀牛膝）、四川（川牛膝）',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用或酒炙',
  contraindication = '孕妇及月经过多者禁用',
  precaution = '中气下陷、脾虚泄泻、下元不固及孕妇禁用',
  incompatibility = NULL,
  identification = '细长圆柱形，稍弯曲，表面灰黄色或淡棕色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '牛膝' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0090',
  latin_name = 'Leonurus japonicus Houtt.',
  source = '唇形科植物益母草的新鲜或干燥地上部分',
  origin = '全国各地均产',
  dosage_usage = '煎服。鲜品加倍',
  dosage_note = NULL,
  processing_method = '生用或熬膏',
  contraindication = '孕妇禁用',
  precaution = '血虚无瘀者不宜',
  incompatibility = NULL,
  identification = '茎方柱形，上部多分枝，表面灰绿色或黄绿色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '益母草' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0091',
  latin_name = 'Corydalis yanhusuo W.T.Wang',
  source = '罂粟科植物延胡索的干燥块茎',
  origin = '浙江（东阳、磐安）',
  dosage_usage = '煎服。研末吞服每次1.5~3g',
  dosage_note = '研末吞服止痛效佳。醋制增强止痛效果',
  processing_method = '生用或醋炙。醋制止痛力增强',
  contraindication = '孕妇忌用，体虚者慎用',
  precaution = '血热气虚者忌用',
  incompatibility = NULL,
  identification = '不规则扁球形，表面黄色或黄褐色，有不规则网状皱纹',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '延胡索' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0092',
  latin_name = 'Curcuma wenyujin Y.H.Chen et C.Ling',
  source = '姜科植物温郁金的干燥块根',
  origin = '浙江（温州）、四川、广西',
  dosage_usage = '煎服',
  dosage_note = '醋炙入肝经止痛效佳',
  processing_method = '生用或醋炙',
  contraindication = '阴虚失血及无气滞血瘀者忌服',
  precaution = '畏丁香。不宜与丁香同用',
  incompatibility = '畏丁香',
  identification = '长卵形或卵圆形，表面灰黄色或灰棕色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '郁金' AND deleted = 0;

-- ==================== 化痰止咳平喘药 ====================

UPDATE herb_knowledge SET
  herb_code = 'HK0093',
  latin_name = 'Pinellia ternata (Thunb.) Breit.',
  source = '天南星科植物半夏的干燥块茎',
  origin = '四川、湖北、安徽',
  dosage_usage = '煎服。一般用制半夏',
  dosage_note = '生品有毒，须炮制后用',
  processing_method = '清半夏、姜半夏、法半夏',
  contraindication = '阴虚燥咳及出血者禁用',
  precaution = '反乌头。不宜与乌头类同用（十八反）',
  incompatibility = '反乌头（十八反）',
  identification = '类球形，表面白色或浅黄色，顶端有凹陷的茎痕',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '半夏' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0094',
  latin_name = 'Arisaema erubescens (Wall.) Schott',
  source = '天南星科植物天南星的干燥块茎',
  origin = '四川、河南、贵州',
  dosage_usage = '煎服。一般用制南星',
  dosage_note = '生品有毒，须炮制后用',
  processing_method = '制南星、胆南星',
  contraindication = '阴虚燥痰者禁用，孕妇慎用',
  precaution = '反乌头。生品有毒勿内服',
  incompatibility = '反乌头（十八反）',
  identification = '扁球形，表面类白色或淡棕色，顶端有凹陷的茎痕',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '天南星' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0095',
  latin_name = 'Fritillaria cirrhosa D.Don',
  source = '百合科植物川贝母的干燥鳞茎',
  origin = '四川、西藏、云南',
  dosage_usage = '煎服。研末冲服每次1~2g',
  dosage_note = '研末冲服效佳，不宜久煎',
  processing_method = '生用',
  contraindication = '脾胃虚寒及寒痰、湿痰者不宜',
  precaution = '反乌头。不宜与乌头类同用（十八反）',
  incompatibility = '反乌头（十八反）',
  identification = '类圆锥形或近球形，外层鳞叶2瓣，大小悬殊（松贝）或大小相近（青贝）',
  is_precious = 1,
  medical_insurance = '甲'
WHERE herb_name = '川贝母' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0096',
  latin_name = 'Fritillaria thunbergii Miq.',
  source = '百合科植物浙贝母的干燥鳞茎',
  origin = '浙江（象山）',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用',
  contraindication = '寒痰、湿痰者不宜',
  precaution = '反乌头（十八反）',
  incompatibility = '反乌头（十八反）',
  identification = '鳞茎由2枚鳞叶组成，略呈肾形，表面类白色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '浙贝母' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0097',
  latin_name = 'Peucedanum praeruptorum Dunn',
  source = '伞形科植物白花前胡的干燥根',
  origin = '浙江、湖南、四川',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用或蜜炙',
  contraindication = '阴虚咳嗽、寒饮咳喘者慎用',
  precaution = NULL,
  incompatibility = '畏藜芦',
  identification = '不规则圆锥形，外皮棕褐色至暗褐色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '前胡' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0098',
  latin_name = 'Platycodon grandiflorus (Jacq.) A.DC.',
  source = '桔梗科植物桔梗的干燥根',
  origin = '安徽、河南、湖北',
  dosage_usage = '煎服',
  dosage_note = '用量过大易致恶心呕吐',
  processing_method = '生用或蜜炙',
  contraindication = '阴虚久嗽及咳血者不宜',
  precaution = '用量过大可致恶心呕吐',
  incompatibility = '畏白及、龙眼、龙胆',
  identification = '长圆柱形或略呈纺锤形，表面白色或淡黄白色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '桔梗' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0099',
  latin_name = 'Prunus armeniaca L.',
  source = '蔷薇科植物山杏的干燥成熟种子',
  origin = '内蒙古、吉林、辽宁',
  dosage_usage = '煎服，捣碎入煎',
  dosage_note = '捣碎入煎。有小毒，不宜过量',
  processing_method = '生用或炒用',
  contraindication = '阴虚咳嗽及大便溏泄者禁用',
  precaution = '有小毒，婴儿慎用',
  incompatibility = '恶黄芪、黄芩、葛根',
  identification = '扁心形，表面黄棕色至深棕色，一端尖另一端钝圆',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '杏仁' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0100',
  latin_name = 'Stemona japonica (Bl.) Miq.',
  source = '百部科植物直立百部的干燥块根',
  origin = '安徽、江苏、浙江',
  dosage_usage = '煎服。外用适量',
  dosage_note = NULL,
  processing_method = '生用或蜜炙。蜜炙润肺止咳力强',
  contraindication = '脾胃虚弱者慎用',
  precaution = '多服久服伤胃',
  incompatibility = NULL,
  identification = '纺锤形，两端细，表面黄白色或浅棕黄色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '百部' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0101',
  latin_name = 'Aster tataricus L.f.',
  source = '菊科植物紫菀的干燥根和根茎',
  origin = '河北、安徽、黑龙江',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用或蜜炙',
  contraindication = '有实热者慎用',
  precaution = NULL,
  incompatibility = '恶天雄、瞿麦、雷丸、远志',
  identification = '根茎不规则块状，顶端有茎及叶的残基，根细长',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '紫苑' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0102',
  latin_name = 'Tussilago farfara L.',
  source = '菊科植物款冬的干燥花蕾',
  origin = '河南、甘肃、山西',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用或蜜炙',
  contraindication = '肺痈咳血者不宜',
  precaution = NULL,
  incompatibility = '恶皂荚、硝石、玄参；畏贝母、连翘、麻黄、黄芩、黄连、青葙',
  identification = '长圆棒状，花蕾未开放，外面有多层苞片',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '款冬花' AND deleted = 0;

-- ==================== 安神药 ====================

UPDATE herb_knowledge SET
  herb_code = 'HK0103',
  latin_name = 'Ziziphus jujuba Mill. var. spinosa (Bunge) Hu ex H.F.Chou',
  source = '鼠李科植物酸枣的干燥成熟种子',
  origin = '河北、陕西、辽宁',
  dosage_usage = '煎服',
  dosage_note = '生用治多眠，炒用治不眠',
  processing_method = '生用或炒用。生用疏肝，炒用安神',
  contraindication = '有实邪郁火者慎用',
  precaution = '有实热者及滑泄者忌用',
  incompatibility = '恶防己',
  identification = '扁圆形或扁椭圆形，表面紫红色或紫褐色，有光泽',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '酸枣仁' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0104',
  latin_name = 'Polygala tenuifolia Willd.',
  source = '远志科植物远志的干燥根',
  origin = '山西、陕西、河北',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用或制用（甘草水炙）',
  contraindication = '心肾有火、阴虚阳亢者忌服',
  precaution = '胃炎及胃溃疡患者慎用，刺激性较强',
  incompatibility = '畏珍珠、藜芦、齐蛤',
  identification = '圆柱形，略弯曲，表面灰黄色至灰棕色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '远志' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0105',
  latin_name = 'Platycladus orientalis (L.) Franco',
  source = '柏科植物侧柏的干燥成熟种仁',
  origin = '山东、河南、河北',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用',
  contraindication = '便溏及痰多者慎用',
  precaution = '大便溏薄者忌服',
  incompatibility = '畏菊花、羊蹄草',
  identification = '长卵形或椭圆形，表面黄白色或淡黄棕色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '柏子仁' AND deleted = 0;

-- ==================== 平肝息风药 ====================

UPDATE herb_knowledge SET
  herb_code = 'HK0106',
  latin_name = 'Gastrodia elata Bl.',
  source = '兰科植物天麻的干燥块茎',
  origin = '四川、云南、贵州',
  dosage_usage = '煎服',
  dosage_note = '研末冲服每次1~1.5g',
  processing_method = '生用',
  contraindication = '气血虚甚者慎用',
  precaution = '津液衰少、血虚、阴虚者慎用',
  incompatibility = NULL,
  identification = '长椭圆形，略扁，表面黄白色至淡黄棕色，有纵皱纹及潜伏芽排列的横环纹',
  is_precious = 1,
  medical_insurance = '甲'
WHERE herb_name = '天麻' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0107',
  latin_name = 'Uncaria rhynchophylla (Miq.) Miq. ex Havil.',
  source = '茜草科植物钩藤的干燥带钩茎枝',
  origin = '广西、广东、湖南',
  dosage_usage = '煎服，后下',
  dosage_note = '后下，不宜久煎。久煎则有效成分破坏',
  processing_method = '生用',
  contraindication = '无风热者不宜',
  precaution = '不宜久煎',
  incompatibility = NULL,
  identification = '茎枝圆柱形或类方柱形，节上生有向下弯曲的双钩或单钩',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '钩藤' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0108',
  latin_name = 'Haliotis diversicolor Reeve',
  source = '鲍科动物杂色鲍的贝壳',
  origin = '广东、福建、辽宁',
  dosage_usage = '煎服，先煎',
  dosage_note = '先煎，打碎入煎',
  processing_method = '生用或煅用',
  contraindication = '脾胃虚寒者慎用',
  precaution = NULL,
  incompatibility = NULL,
  identification = '耳形，表面暗红色，内表面有珍珠样光泽',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '石决明' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0109',
  latin_name = 'Pteria martensii (Dunker)',
  source = '珍珠贝科动物马氏珍珠贝的贝壳',
  origin = '广东、广西、海南',
  dosage_usage = '煎服，先煎',
  dosage_note = '先煎，打碎入煎',
  processing_method = '生用或煅用',
  contraindication = '脾胃虚寒及无实热者不宜',
  precaution = NULL,
  incompatibility = NULL,
  identification = '不规则片状，外表面类白色或灰白色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '珍珠母' AND deleted = 0;

-- ==================== 补气药 ====================

UPDATE herb_knowledge SET
  herb_code = 'HK0110',
  latin_name = 'Panax ginseng C.A.Mey.',
  source = '五加科植物人参的干燥根和根茎',
  origin = '吉林、辽宁、黑龙江',
  dosage_usage = '另煎兑服。研末吞服每次1~2g',
  dosage_note = '宜另煎兑服，不宜与萝卜、浓茶同服',
  processing_method = '生晒参、红参、糖参。生晒参偏凉补，红参偏温补',
  contraindication = '实证、热证忌服。不宜与藜芦、五灵脂同用',
  precaution = '反藜芦，畏五灵脂。不宜与萝卜同食',
  incompatibility = '反藜芦，畏五灵脂（十九畏）',
  identification = '主根呈纺锤形或圆柱形，表面灰黄色，上部有紧密的环纹（芦头）',
  is_precious = 1,
  medical_insurance = '甲'
WHERE herb_name = '人参' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0111',
  latin_name = 'Astragalus membranaceus (Fisch.) Bge.',
  source = '豆科植物蒙古黄芪的干燥根',
  origin = '内蒙古、山西、甘肃',
  dosage_usage = '煎服',
  dosage_note = '补气升阳宜炙用，固表利水宜生用',
  processing_method = '生用或蜜炙',
  contraindication = '表实邪盛、气滞湿阻、食积停滞者禁用',
  precaution = '阴虚阳亢及疮疡初起或溃后热毒尚盛者忌用',
  incompatibility = '反藜芦',
  identification = '圆柱形，极少有分枝，表面淡棕黄色或淡棕褐色，质硬而韧，断面纤维性强',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '黄芪' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0112',
  latin_name = 'Codonopsis pilosula (Franch.) Nannf.',
  source = '桔梗科植物党参的干燥根',
  origin = '甘肃、山西、四川',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用或蜜炙',
  contraindication = '有实邪者忌用',
  precaution = '不宜与藜芦同用。反藜芦',
  incompatibility = '反藜芦',
  identification = '长圆柱形，根头有多数疣状突起的茎痕及芽，表面灰黄色至灰棕色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '党参' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0113',
  latin_name = 'Atractylodes macrocephala Koidz.',
  source = '菊科植物白术的干燥根茎',
  origin = '浙江（於术）、安徽、湖北',
  dosage_usage = '煎服',
  dosage_note = '健脾止泻宜炒用，燥湿利水宜生用',
  processing_method = '生用或土炒、麸炒',
  contraindication = '阴虚内热者慎用',
  precaution = '阴虚燥渴、气滞胀满者忌服',
  incompatibility = NULL,
  identification = '不规则肥厚团块，表面灰黄色或灰棕色，有瘤状突起及断续的纵皱纹',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '白术' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0114',
  latin_name = 'Dioscorea opposita Thunb.',
  source = '薯蓣科植物薯蓣的干燥根茎',
  origin = '河南（怀山药）、河北、山西',
  dosage_usage = '煎服',
  dosage_note = '补阴宜生用，健脾止泻宜炒用',
  processing_method = '生用或麸炒',
  contraindication = '湿盛中满或有实邪积滞者禁用',
  precaution = NULL,
  incompatibility = '忌铁器',
  identification = '圆柱形，表面白色或淡黄色，纵切面白色，粉性',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '山药' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0115',
  latin_name = 'Glycyrrhiza uralensis Fisch.',
  source = '豆科植物甘草的干燥根和根茎',
  origin = '内蒙古、甘肃、新疆',
  dosage_usage = '煎服',
  dosage_note = '不宜与海藻、大戟、甘遂、芫花同用',
  processing_method = '生用或蜜炙。生用清热解毒，炙用补中益气',
  contraindication = '湿盛胀满者忌服',
  precaution = '反大戟、甘遂、芫花、海藻（十八反）。久服较大剂量可致水钠潴留',
  incompatibility = '反大戟、甘遂、芫花、海藻（十八反）',
  identification = '圆柱形，表面红棕色或灰棕色，断面黄白色，纤维性，有粉性',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '甘草' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0116',
  latin_name = 'Ziziphus jujuba Mill.',
  source = '鼠李科植物枣的干燥成熟果实',
  origin = '河南、河北、山东',
  dosage_usage = '煎服',
  dosage_note = '劈开入煎',
  processing_method = '生用',
  contraindication = '湿盛中满、食积及虫积者不宜',
  precaution = '痰热咳嗽者不宜',
  incompatibility = NULL,
  identification = '椭圆形或球形，表面暗红色，略带光泽，有不规则皱缩',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '大枣' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0117',
  latin_name = 'Panax quinquefolium L.',
  source = '五加科植物西洋参的干燥根',
  origin = '进口（美国、加拿大）或国产（吉林、北京）',
  dosage_usage = '另煎兑服。研末冲服每次1~3g',
  dosage_note = '另煎兑服或研末冲服',
  processing_method = '生用',
  contraindication = '中阳衰微、胃有寒湿者忌用',
  precaution = '不宜与藜芦同用。反藜芦',
  incompatibility = '反藜芦',
  identification = '纺锤形或圆柱形，表面浅黄褐色或灰黄色',
  is_precious = 1,
  medical_insurance = '甲'
WHERE herb_name = '西洋参' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0118',
  latin_name = 'Pseudostellaria heterophylla (Miq.) Pax',
  source = '石竹科植物孩儿参的干燥块根',
  origin = '贵州、福建、安徽',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用',
  contraindication = '邪实者忌用',
  precaution = NULL,
  incompatibility = NULL,
  identification = '细长纺锤形或细长条形，表面黄白色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '太子参' AND deleted = 0;

-- ==================== 补血药 ====================

UPDATE herb_knowledge SET
  herb_code = 'HK0119',
  latin_name = 'Angelica sinensis (Oliv.) Diels',
  source = '伞形科植物当归的干燥根',
  origin = '甘肃（岷县）、云南、四川',
  dosage_usage = '煎服',
  dosage_note = '当归头止血，当归身补血，当归尾活血，全当归活血补血',
  processing_method = '生用或酒炙',
  contraindication = '湿盛中满、大便溏泄者慎用',
  precaution = '湿阻中满及大便溏者忌服',
  incompatibility = '恶南茹，畏菖蒲、海藻、牡蒙',
  identification = '略呈圆柱形，根上端称归头，主根称归身，支根称归尾，表面黄棕色至棕褐色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '当归' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0120',
  latin_name = 'Rehmannia glutinosa Libosch.',
  source = '玄参科植物地黄的块根经加工炮制而成',
  origin = '河南（怀庆府）',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '酒蒸至黑润',
  contraindication = '脾虚痰多、气滞食少、腹满便溏者不宜',
  precaution = '性质黏腻，碍胃滞脾。砂仁可佐之',
  incompatibility = NULL,
  identification = '不规则块片状，表面乌黑色，有光泽，黏性大',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '熟地黄' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0121',
  latin_name = 'Paeonia lactiflora Pall.',
  source = '毛茛科植物芍药的干燥根',
  origin = '浙江（杭白芍）、安徽（亳芍）、四川（川白芍）',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用或炒用、酒炙',
  contraindication = '阳衰虚寒之证不宜',
  precaution = '不宜与藜芦同用。反藜芦',
  incompatibility = '反藜芦',
  identification = '圆柱形，平直或稍弯曲，表面类白色或淡红棕色，光洁或有纵皱纹',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '白芍' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0122',
  latin_name = 'Polygonum multiflorum Thunb.',
  source = '蓼科植物何首乌的干燥块根',
  origin = '贵州、四川、广东',
  dosage_usage = '煎服',
  dosage_note = '制用补益，生用润肠通便、解毒',
  processing_method = '生用或制用（黑豆汁蒸制）',
  contraindication = '大便溏泄、湿痰较重者不宜',
  precaution = '忌铁器。有肝毒性报道，不宜长期大量使用',
  incompatibility = '忌铁器，忌猪血、无鳞鱼',
  identification = '团块状或不规则纺锤形，表面红棕色或红褐色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '何首乌' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0123',
  latin_name = 'Equus asinus L.',
  source = '马科动物驴的干燥皮或鲜皮经煎煮浓缩制成的固体胶',
  origin = '山东（东阿）',
  dosage_usage = '烊化兑服',
  dosage_note = '烊化兑服，不宜入煎剂同煎',
  processing_method = '蛤粉炒或蒲黄炒',
  contraindication = '脾胃虚弱、消化不良者慎用',
  precaution = '性滞腻，消化不良及有表证者慎用',
  incompatibility = NULL,
  identification = '长方形块状，黑褐色，有光泽，质硬而脆，断面光亮',
  is_precious = 1,
  medical_insurance = '甲'
WHERE herb_name = '阿胶' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0124',
  latin_name = 'Dimocarpus longan Lour.',
  source = '无患子科植物龙眼的假种皮',
  origin = '广东、广西、福建',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用',
  contraindication = '内有痰火及湿滞停饮者忌服',
  precaution = '湿盛者不宜',
  incompatibility = NULL,
  identification = '纵向破裂的不规则薄片，棕黄色至棕褐色，半透明',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '龙眼肉' AND deleted = 0;

-- ==================== 补阴药 ====================

UPDATE herb_knowledge SET
  herb_code = 'HK0125',
  latin_name = 'Lycium barbarum L.',
  source = '茄科植物宁夏枸杞的干燥成熟果实',
  origin = '宁夏（中宁）、甘肃、青海',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用',
  contraindication = '外邪实热、脾虚有湿及泄泻者忌服',
  precaution = '脾胃虚弱者不宜多用',
  incompatibility = NULL,
  identification = '类纺锤形或椭圆形，表面红色或暗红色，略有皱缩',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '枸杞子' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0126',
  latin_name = 'Ophiopogon japonicus (L.f.) Ker-Gawl.',
  source = '百合科植物麦冬的干燥块根',
  origin = '四川（绵阳）、浙江（杭麦冬）',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用',
  contraindication = '脾胃虚寒泄泻、胃有痰饮湿浊者忌用',
  precaution = '风寒咳嗽者不宜使用',
  incompatibility = '畏款冬、苦瓠；反苍耳',
  identification = '纺锤形，两端略尖，表面黄白色或淡黄色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '麦冬' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0127',
  latin_name = 'Asparagus cochinchinensis (Lour.) Merr.',
  source = '百合科植物天冬的干燥块根',
  origin = '贵州、四川、广西',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用',
  contraindication = '虚寒泄泻及外感风寒致嗽者忌服',
  precaution = '脾胃虚寒、食少便溏者慎用',
  incompatibility = '畏曾青',
  identification = '长纺锤形，略弯曲，表面黄白色至淡黄棕色，半透明',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '天冬' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0128',
  latin_name = 'Dendrobium nobile Lindl.',
  source = '兰科植物金钗石斛或铁皮石斛的干燥茎',
  origin = '四川、贵州、云南',
  dosage_usage = '煎服。鲜品用量加倍',
  dosage_note = '鲜品效佳。宜先煎',
  processing_method = '生用。鲜用或干用',
  contraindication = '温热病早期阴未伤者及湿温病未化燥者忌服',
  precaution = '脾胃虚寒者慎用',
  incompatibility = '畏巴豆、僵蚕、雷丸',
  identification = '鲜品圆柱形或扁圆柱形，表面黄绿色（铁皮石斛干品呈螺旋状弹簧形）',
  is_precious = 1,
  medical_insurance = '甲'
WHERE herb_name = '石斛' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0129',
  latin_name = 'Polygonatum odoratum (Mill.) Druce',
  source = '百合科植物玉竹的干燥根茎',
  origin = '湖南、河南、江苏',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用',
  contraindication = '脾虚痰多及气滞者忌服',
  precaution = '痰湿气滞者不宜',
  incompatibility = '畏卤咸',
  identification = '长圆柱形，略扁，表面黄白色或淡黄棕色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '玉竹' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0130',
  latin_name = 'Lilium brownii F.E.Brown var. viridulum Baker',
  source = '百合科植物卷丹或百合的干燥肉质鳞叶',
  origin = '湖南、浙江、安徽',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用或蜜炙',
  contraindication = '风寒咳嗽及中寒便溏者忌服',
  precaution = NULL,
  incompatibility = NULL,
  identification = '长椭圆形，表面类白色、淡棕黄色或微带紫色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '百合' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0131',
  latin_name = 'Ligustrum lucidum Ait.',
  source = '木犀科植物女贞的干燥成熟果实',
  origin = '浙江、江苏、湖南',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用或酒制',
  contraindication = '脾胃虚寒泄泻及阳虚者忌服',
  precaution = '脾胃虚寒者慎用',
  incompatibility = NULL,
  identification = '卵形、椭圆形或肾形，表面黑紫色或灰黑色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '女贞子' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0132',
  latin_name = 'Eclipta prostrata L.',
  source = '菊科植物鳢肠的干燥地上部分',
  origin = '全国各地均产',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用',
  contraindication = '脾肾虚寒者慎用',
  precaution = '大便溏薄者慎用',
  incompatibility = NULL,
  identification = '全草缠绕成团，茎圆柱形，表面绿褐色或墨绿色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '墨旱莲' AND deleted = 0;

-- ==================== 补阳药 ====================

UPDATE herb_knowledge SET
  herb_code = 'HK0133',
  latin_name = 'Cervus nippon Temminck',
  source = '鹿科动物梅花鹿的雄鹿未骨化密生茸毛的幼角',
  origin = '吉林、辽宁、黑龙江',
  dosage_usage = '研末冲服，每次1~2g',
  dosage_note = '宜从小量开始，缓缓增加，不宜骤用大量',
  processing_method = '燎去毛，切片。血片（蜡片）效力最强',
  contraindication = '阴虚阳亢、血热出血及胃火盛者禁用',
  precaution = '外感热病者禁用',
  incompatibility = NULL,
  identification = '圆柱状分枝，具1~2个分枝，外皮红棕色或棕色，密被黄棕色或灰褐色茸毛',
  is_precious = 1,
  medical_insurance = '甲'
WHERE herb_name = '鹿茸' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0134',
  latin_name = 'Epimedium brevicornum Maxim.',
  source = '小檗科植物淫羊藿的干燥叶',
  origin = '四川、贵州、湖北',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用或炙用（羊脂油炙）',
  contraindication = '阴虚火旺者禁用',
  precaution = '阴虚火旺者忌用',
  incompatibility = NULL,
  identification = '三出复叶，小叶片卵圆形，边缘有锯齿',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '淫羊藿' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0135',
  latin_name = 'Morinda officinalis How',
  source = '茜草科植物巴戟天的干燥根',
  origin = '广东、广西、福建',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用或盐水炙',
  contraindication = '阴虚火旺者忌服',
  precaution = '阴虚火旺、湿热者不宜',
  incompatibility = NULL,
  identification = '扁圆柱形，略弯曲，表面灰黄色或暗灰色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '巴戟天' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0136',
  latin_name = 'Eucommia ulmoides Oliv.',
  source = '杜仲科植物杜仲的干燥树皮',
  origin = '四川、贵州、湖北',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用或盐水炙',
  contraindication = '阴虚火旺者慎服',
  precaution = NULL,
  incompatibility = '恶蛇蜕、元参',
  identification = '板片状或两边稍卷，外表面淡棕色或灰褐色，折断面有细密银白色橡胶丝相连',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '杜仲' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0137',
  latin_name = 'Dipsacus asper Wall. ex Henry',
  source = '川续断科植物川续断的干燥根',
  origin = '四川、湖北、贵州',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用、酒炙或盐水炙',
  contraindication = '初痢勿用，怒气郁者禁用',
  precaution = NULL,
  incompatibility = NULL,
  identification = '圆柱形，略扁，表面灰褐色或黄褐色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '续断' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0138',
  latin_name = 'Cistanche deserticola Y.C.Ma',
  source = '列当科植物肉苁蓉的干燥带鳞叶的肉质茎',
  origin = '内蒙古、新疆、甘肃',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用或酒制',
  contraindication = '阴虚火旺、大便泄泻者不宜',
  precaution = '相火偏旺、胃弱便溏、实热便结者忌服',
  incompatibility = NULL,
  identification = '扁圆柱形，表面棕褐色或灰棕色，密被覆瓦状排列的肉质鳞叶',
  is_precious = 1,
  medical_insurance = '甲'
WHERE herb_name = '肉苁蓉' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0139',
  latin_name = 'Cuscuta chinensis Lam.',
  source = '旋花科植物菟丝子的干燥成熟种子',
  origin = '辽宁、吉林、河北',
  dosage_usage = '煎服',
  dosage_note = '宜包煎',
  processing_method = '生用或盐水炙',
  contraindication = '阴虚火旺、大便燥结、小便短赤者不宜',
  precaution = '孕妇慎用',
  incompatibility = NULL,
  identification = '类球形，表面灰棕色或黄褐色，微粗糙',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '菟丝子' AND deleted = 0;

-- ==================== 收涩药 ====================

UPDATE herb_knowledge SET
  herb_code = 'HK0140',
  latin_name = 'Schisandra chinensis (Turcz.) Baill.',
  source = '木兰科植物五味子的干燥成熟果实',
  origin = '辽宁、吉林、黑龙江',
  dosage_usage = '煎服',
  dosage_note = '捣碎入煎',
  processing_method = '生用或醋制、酒制',
  contraindication = '外有表邪、内有实热者不宜',
  precaution = '咳嗽初起及麻疹初发者忌服',
  incompatibility = '恶葳蕤；畏萝藦',
  identification = '不规则球形或扁球形，表面红色、紫红色或暗红色，皱缩',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '五味子' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0141',
  latin_name = 'Cornus officinalis Sieb. et Zucc.',
  source = '山茱萸科植物山茱萸的干燥成熟果肉',
  origin = '河南、浙江、陕西',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用或酒制',
  contraindication = '命门火炽、湿热者忌服',
  precaution = '素有湿热、小便淋涩者忌服',
  incompatibility = '恶桔梗、防风、防己',
  identification = '不规则的片状或囊状，表面紫红色至紫黑色，皱缩有光泽',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '山茱萸' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0142',
  latin_name = 'Nelumbo nucifera Gaertn.',
  source = '睡莲科植物莲的干燥成熟种子',
  origin = '湖南、福建、江苏',
  dosage_usage = '煎服',
  dosage_note = '去心（莲子心）另用，清心安神',
  processing_method = '生用或炒用',
  contraindication = '中满痞胀及大便燥结者忌服',
  precaution = NULL,
  incompatibility = NULL,
  identification = '略呈椭圆形或类球形，表面浅黄棕色至红棕色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '莲子' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0143',
  latin_name = 'Euryale ferox Salisb.',
  source = '睡莲科植物芡的干燥成熟种仁',
  origin = '江苏、湖南、安徽',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用或麸炒',
  contraindication = '大小便不利者禁服',
  precaution = NULL,
  incompatibility = NULL,
  identification = '类球形，表面有棕红色内种皮残留',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '芡实' AND deleted = 0;

-- ==================== 其他常用药 ====================

UPDATE herb_knowledge SET
  herb_code = 'HK0144',
  latin_name = 'Pheretima aspergillum (E.Perrier)',
  source = '巨蚓科动物参环毛蚓的干燥体',
  origin = '广东、广西、福建',
  dosage_usage = '煎服',
  dosage_note = '鲜品效果更佳',
  processing_method = '生用或鲜用',
  contraindication = '脾胃虚寒者不宜',
  precaution = NULL,
  incompatibility = NULL,
  identification = '长条状薄片，弯曲，边缘略卷，全体具环节',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '地龙' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0145',
  latin_name = 'Buthus martensii Karsch',
  source = '钳蝎科动物东亚钳蝎的干燥体',
  origin = '河南、山东、河北',
  dosage_usage = '煎服。研末吞服每次0.6~1g',
  dosage_note = '有毒，用量宜小',
  processing_method = '生用',
  contraindication = '血虚生风者忌服，孕妇禁用',
  precaution = '有毒，不宜过量',
  incompatibility = NULL,
  identification = '头胸部与前腹部呈扁平长椭圆形，后腹部呈尾状',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '全蝎' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0146',
  latin_name = 'Scolopendra subspinipes mutilans L.Koch',
  source = '蜈蚣科动物少棘巨蜈蚣的干燥体',
  origin = '湖北、浙江、江苏',
  dosage_usage = '煎服。研末吞服每次0.6~1g',
  dosage_note = '有毒，用量宜小',
  processing_method = '生用',
  contraindication = '血虚生风者忌服，孕妇禁用',
  precaution = '有毒，不宜过量。孕妇忌用',
  incompatibility = '畏蜘蛛、蛞蝓',
  identification = '扁平长条形，由头部和躯干部组成，全体22个环节',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '蜈蚣' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0147',
  latin_name = 'Bombyx mori L.',
  source = '蚕蛾科昆虫家蚕的幼虫感染白僵菌而致死的干燥体',
  origin = '浙江、江苏、四川',
  dosage_usage = '煎服',
  dosage_note = NULL,
  processing_method = '生用或炒用',
  contraindication = '心虚不宁、血虚生风者慎用',
  precaution = NULL,
  incompatibility = NULL,
  identification = '略呈圆柱形，多弯曲皱缩，表面灰黄色，被有白色粉霜状气生菌丝',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '白僵蚕' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0148',
  latin_name = 'Succinum',
  source = '古代松科植物的树脂经地层埋藏而成的化石样物质',
  origin = '云南、广西、河南',
  dosage_usage = '研末冲服，每次1.5~3g',
  dosage_note = '不入煎剂，研末冲服',
  processing_method = '生用',
  contraindication = '阴虚内热及无瘀滞者慎用',
  precaution = '阴虚者慎用',
  incompatibility = NULL,
  identification = '不规则块状、颗粒状或多角形，血红色、黄棕色至暗棕色',
  is_precious = 1,
  medical_insurance = '乙'
WHERE herb_name = '琥珀' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0149',
  latin_name = 'Os Draconis (古代哺乳动物化石)',
  source = '古代哺乳动物如象类、犀牛类等的骨骼化石',
  origin = '河南、陕西、山西',
  dosage_usage = '煎服，先煎',
  dosage_note = '先煎，打碎入煎',
  processing_method = '生用或煅用',
  contraindication = '有湿热者不宜',
  precaution = '湿热积滞者忌用',
  incompatibility = '畏石膏；忌鱼',
  identification = '不规则块状，表面白色、灰白色或黄白色，较光滑',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '龙骨' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0150',
  latin_name = 'Ostrea gigas Thunberg',
  source = '牡蛎科动物长牡蛎的贝壳',
  origin = '广东、福建、山东',
  dosage_usage = '煎服，先煎',
  dosage_note = '先煎，打碎入煎',
  processing_method = '生用或煅用。收敛固涩宜煅用',
  contraindication = NULL,
  precaution = NULL,
  incompatibility = '恶细辛、吴茱萸；畏麻黄',
  identification = '长片状，不规则，上壳较小，下壳较大，表面灰紫色或灰褐色',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '牡蛎' AND deleted = 0;

UPDATE herb_knowledge SET
  herb_code = 'HK0151',
  latin_name = 'Magnetitum (Fe3O4)',
  source = '氧化物类矿物尖晶石族磁铁矿的矿石',
  origin = '河北、山东、辽宁',
  dosage_usage = '煎服，先煎',
  dosage_note = '先煎，打碎入煎',
  processing_method = '生用或煅用',
  contraindication = '脾胃虚弱者慎用',
  precaution = '不宜过量久服',
  incompatibility = NULL,
  identification = '不规则块状，灰黑色或棕褐色，具金属光泽，能被磁铁吸引',
  is_precious = 0,
  medical_insurance = '甲'
WHERE herb_name = '磁石' AND deleted = 0;