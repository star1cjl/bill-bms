<#import "/base/base.ftl" as base>
<#import "/base/dict.ftl" as dict>

<@base.html "客户任务大厅">
<link href="${ctx}/static/css/Message/font-awesome.min.css" rel="stylesheet">
<link href="${ctx}/static/css/Message/animate.css" rel="stylesheet">
<link href="${ctx}/static/css/Message/custom.css" rel="stylesheet">
<script src="//rawgit.com/hhurz/tableExport.jquery.plugin/master/tableExport.js"></script>
<#--<link href="${ctx}/static/css/Message/Index.css" rel="stylesheet">-->
<script src="${ctx}/static/js/public/bootstrap-editable.js"></script>
<script src="${ctx}/static/js/public/bootstrap-table-editable.js"></script>
<link href="${ctx}/static/css/bill/KeyWordsRanking.css" rel="stylesheet">
<div class="Navs">
    <div class="nav_L left">
        <i class="fa fa-home">&nbsp;</i><span>优化工具</span> > <span>关键词指数查询</span>
    </div>
    <div class="nav_R right" id="divQx">
        <div class="search">
            <span>&nbsp;<i class="fa fa-search"></i>&nbsp;查询</span>
        </div>
    </div>
    <div class="cls">
    </div>
</div>
<div class="Navs2">
    <div class="nav_R2 right" >
        <div>
            关键词:
        </div>
        <input id="keywords" name="keywords" class="form-control" value="" style="width: 150px;" type="text">
        <span id="searchButton">查询</span>
    </div>
    <div class="cls">
    </div>
</div>

<div class="row">
    <div class="col-md-12">
        <div class="panel panel-primary" id="list-panel" style="border-color: #eeeeee;">
            <div class="panel-body">
                <div class="panel-nav">
                    <div class="Nav_Left">&nbsp;<i class="fa fa-paper-plane"></i>&nbsp;关键词指数查询
                    </div>
                    <div class="Nav_Right">


                    </div>
                    <div class="cls"></div>
                </div>

                <table id="myTable" class="table table-striped  table-condensed table-responsive"
                       style="width:100%;font-size: 13px;font-family: " 微软雅黑
                "></table>
                <style>
                    table a{
                        color: #0099CC;
                    }
                </style>
            </div>

        </div>
    </div>
</div>

<script type="text/javascript">
    //显示搜索内容
    $(".search").click(function () {
        if ($(".Navs2").css("display") == "block") {
            $(".Navs2").slideUp();
        }
        else {
            $(".Navs2").slideDown();

        }
    })
    var keywords=null;
    $("#searchButton").click(function () {
        if($("#keywords").val()!="")
        {
            keywords=$("#keywords").val();
        }
        else
        {
            keywords=null;
        }
        $('#myTable').bootstrapTable('refresh');
    })
    var index1;
    $(function () {
        MissionHall.init();
    });
    var MissionHall = {
        init: function () {
            //1.初始化Table
            var oTable = new MissionHall.TableInit();
            oTable.Init();
            //划分按钮
            $("#divide").on("click", MissionHall.divideClick);
            $("#confirm").on("click", MissionHall.confirmDistributor);
        },
        TableInit: function () {
            var oTableInit = new Object();
            //初始化Table
            oTableInit.Init = function () {
                $('#myTable').bootstrapTable({
                    url: CTX + '/optimizationTool/GetKeywordsList',         //请求后台的URL（*）
                    method: 'get',                      //请求方式（*）
                    toolbar: '#toolbar',                //工具按钮用哪个容器
                    striped: true,                      //是否显示行间隔色
                    cache: false,                       //是否使用缓存，默认为true，所以一般情况下需要设置一下这个属性（*）
                    pagination: true,                   //是否显示分页（*）
                    sortable: true,                     //是否启用排序
                    sortOrder: "asc",                   //排序方式
                    queryParams: oTableInit.queryParams,//传递参数（*）
                    sidePagination: "server",           //分页方式：client客户端分页，server服务端分页（*）
                    //queryParamsType: "",
                    pageNumber: 1,                       //初始化加载第一页，默认第一页
                    pageSize: 50,                       //每页的记录行数（*）
                    pageList: [100, 300, 500, 1000],        //可供选择的每页的行数（*）
                    showColumns: false,                  //是否显示所有的列
                    showRefresh: false,                  //是否显示刷新按钮
                    minimumCountColumns: 2,             //最少允许的列数
                    clickToSelect: true,                //是否启用点击选中行
                    //height: 500,                        //行高，如果没有设置height属性，表格自动根据记录条数觉得表格高度
                    uniqueId: "ID",                     //每一行的唯一标识，一般为主键列
                    showToggle: false,                    //是否显示详细视图和列表视图的切换按钮
                    cardView: false,                    //是否显示详细视图
                    detailView: false,                   //是否显示父子表

                    showExport: true,  //是否显示导出

                    columns: [
                        {
                            field: 'id',
                            title: '编号',
                            align: 'center',
                            valign: 'middle',
                            visible: false
                        },
                        {
                            field: 'Keywords',
                            align: 'center',
                            valign: 'middle',
                            title: '关键词',
                        },
                        {
                            field: 'IndexBaiduAll',
                            align: 'left',
                            valign: 'middle',
                            title: '百度整体指数',
                            //events: ReadMailEvents

                        },
                        {
                            field: 'IndexBaiduWap',
                            title: '百度手机指数',
                            align: 'center',
                            valign: 'middle'
                        },
                        {
                            field: 'IndexSoAll',
                            title: '360指数',
                            align: 'center',
                            valign: 'middle'
                        },
                        {
                            field: 'BaiduCollectionCount',
                            title: '收录量',
                            align: 'center',
                            valign: 'middle',
                            visible: false

                        },
                        {
                            field: 'BaiduHomepageCount',
                            align: 'center',
                            valign: 'middle',
                            title: '首页数量'
                        },
                        {
                            field: 'Degree',
                            align: 'center',
                            valign: 'middle',
                            title: '优化难度',
                        },
                        {
                            field: 'PriceBaiduPc',
                            align: 'center',
                            valign: 'middle',
                            title: '百度价格',
                            editable: true,
                        },
                        {
                            field: 'PriceBaiduWap',
                            align: 'center',
                            valign: 'middle',
                            title: '百度手机价格',
                            editable: true,
                        },
                        {
                            field: 'PriceSoPc',
                            align: 'center',
                            valign: 'middle',
                            title: '360价格',
                            editable: true,
                        },
                        {
                            field: 'PriceSoWap',
                            align: 'center',
                            valign: 'middle',
                            title: '360手机价格',
                            editable: true,
                        },
                        {
                            field: 'PriceSogouPc',
                            align: 'center',
                            valign: 'middle',
                            title: '搜狗价格',
                            editable: true,
                        },
                        {
                            field: 'PriceSogouWap',
                            align: 'center',
                            valign: 'middle',
                            title: '搜狗手机价格',
                            editable: true,
                        },
                        {
                            field: 'PriceSm',
                            align: 'center',
                            valign: 'middle',
                            title: '神马价格',
                            editable: true,
                        },
                        {
                            field: 'operate',
                            align: 'center',
                            valign: 'middle',
                            title: '修改',
                            formatter: function (value, row, index) {
                                var a = "<span style='color:#4382CF;cursor:pointer;' id='details'>修改</span> ";

                                return a;
                            },
                            events: operateEvents
                        }
                    ]
                });
            };

            function stateFormatter(value, row, index) {
                if (row.orderstate == 4)
                    return {
                        disabled: true,//设置是否可用
                        checked: false//设置选中
                    };
                return value;
            }

            //得到查询的参数
            //oTableInit.queryParams = MissionHall.queryParams;
            oTableInit.queryParams = function (params) {
                var temp = {   //这里的键的名字和控制器的变量名必须一直，这边改动，控制器也需要改成一样的
                    limit: params.limit,   //页面大小
                    offset: params.offset,  //页码
                    keywords:keywords
                };
                return temp;
            }
            window.operateEvents = {
                'click #details': function (e, value, row, index) {
                    $.ajax({
                        type: "post",
                        url: CTX+"/optimizationTool/updateKeyWordsPrice",
                        data: row,
                        dataType: 'JSON',
                        success: function (result) {

                                $('#myTable').bootstrapTable('refresh');
                                alert(result.message);

                        },
                        error: function () {
                            alert('未知错误');
                        },
                        complete: function () {

                        }

                    });
                }

            }
            return oTableInit;
        }
    };
</script>
</@base.html>