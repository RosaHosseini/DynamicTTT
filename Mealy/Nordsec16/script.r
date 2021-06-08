list.of.packages <- c("ggplot2","reshape2","gtools","stringr","scales","effsize","SortableHTMLTables","RColorBrewer","ggpubr","nortest","cowplot")

new.packages <- list.of.packages[!(list.of.packages %in% installed.packages(lib.loc="/home/cdnd1/Rpackages/")[,"Package"])]
if(length(new.packages)) install.packages(new.packages,lib="/home/cdnd1/Rpackages/")
lapply(list.of.packages,require,character.only=TRUE, lib.loc="/home/cdnd1/Rpackages/")

# new.packages <- list.of.packages[!(list.of.packages %in% installed.packages()[,"Package"])]
# if(length(new.packages)) install.packages(new.packages, dependencies = TRUE)
# lapply(list.of.packages,require,character.only=TRUE)

# devtools::install_github("wilkelab/cowplot")
# devtools::install_github("kassambara/ggpubr")
rm(new.packages,list.of.packages)


# Multiple plot function
#
# ggplot objects can be passed in ..., or to plotlist (as a list of ggplot objects)
# - cols:   Number of columns in layout
# - layout: A matrix specifying the layout. If present, 'cols' is ignored.
#
# If the layout is something like matrix(c(1,2,3,3), nrow=2, byrow=TRUE),
# then plot 1 will go in the upper left, 2 will go in the upper right, and
# 3 will go all the way across the bottom.
#
multiplot <- function(..., plotlist=NULL, file, cols=1, layout=NULL) {
  library(grid)
  
  # Make a list from the ... arguments and plotlist
  plots <- c(list(...), plotlist)
  
  numPlots = length(plots)
  
  # If layout is NULL, then use 'cols' to determine layout
  if (is.null(layout)) {
    # Make the panel
    # ncol: Number of columns of plots
    # nrow: Number of rows needed, calculated from # of cols
    layout <- matrix(seq(1, cols * ceiling(numPlots/cols)),
                     ncol = cols, nrow = ceiling(numPlots/cols))
  }
  
  if (numPlots==1) {
    print(plots[[1]])
    
  } else {
    # Set up the page
    grid.newpage()
    pushViewport(viewport(layout = grid.layout(nrow(layout), ncol(layout))))
    
    # Make each plot, in the correct location
    for (i in 1:numPlots) {
      # Get the i,j matrix positions of the regions that contain this subplot
      matchidx <- as.data.frame(which(layout == i, arr.ind = TRUE))
      
      print(plots[[i]], vp = viewport(layout.pos.row = matchidx$row,
                                      layout.pos.col = matchidx$col))
    }
  }
}

## Gives count, mean, standard deviation, standard error of the mean, and confidence interval (default 95%).
##   data: a data frame.
##   measurevar: the name of a column that contains the variable to be summariezed
##   groupvars: a vector containing names of columns that contain grouping variables
##   na.rm: a boolean that indicates whether to ignore NA's
##   conf.interval: the percent range of the confidence interval (default is 95%)
summarySE <- function(data=NULL, measurevar, groupvars=NULL, na.rm=FALSE,
                      conf.interval=.95, .drop=TRUE) {
  library(plyr)
  
  # New version of length which can handle NA's: if na.rm==T, don't count them
  length2 <- function (x, na.rm=FALSE) {
    if (na.rm) sum(!is.na(x))
    else       length(x)
  }
  
  # This does the summary. For each group's data frame, return a vector with
  # N, mean, and sd
  datac <- ddply(data, groupvars, .drop=.drop,
                 .fun = function(xx, col) {
                   c(N    = length2(xx[[col]], na.rm=na.rm),
                     mean = mean   (xx[[col]], na.rm=na.rm),
                     sd   = sd     (xx[[col]], na.rm=na.rm)
                   )
                 },
                 measurevar
  )
  
  # Rename the "mean" column    
  datac <- rename(datac, c("mean" = measurevar))
  
  datac$se <- datac$sd / sqrt(datac$N)  # Calculate standard error of the mean
  
  # Confidence interval multiplier for standard error
  # Calculate t-statistic for confidence interval: 
  # e.g., if conf.interval is .95, use .975 (above/below), and use df=N-1
  ciMult <- qt(conf.interval/2 + .5, datac$N-1)
  datac$ci <- datac$se * ciMult
  
  return(datac)
}

loadTabAsDataFrame <-function(filename){
  data <- read.table(tab_filename, sep="\t", header=TRUE)
  
  reused_lst  <- levels(data$Reused)
  reused_lst  <-reused_lst [! (reused_lst %in% c('N/A'))]
  reused_lst  <- c(c("N/A"),reused_lst)
  data$Reused <- factor(data$Reused, reused_lst)
  
  data$Total_Resets<-data$EQ_Reset+data$MQ_Reset
  data$Total_Symbols<-data$EQ_Symbol+data$MQ_Symbol
  data$AVG_EQ_LEN<-data$EQ_Symbol/data$EQ_Reset
  data$AVG_MQ_LEN<-data$MQ_Symbol/data$MQ_Reset

  # for(model_name in unique(data$Inferred)){
  #   data[((data$Inferred==model_name)&(!(data$Reused=="N/A"))),"EQ_Reset_Percent"]<-data[((data$Inferred==model_name)&(!(data$Reused=="N/A"))),"EQ_Reset"]/data[((data$Inferred==model_name)&((data$Reused=="N/A"))),"EQ_Reset"]
  #   data[((data$Inferred==model_name)&(!(dataunique(data$Inferred)$Reused=="N/A"))),"MQ_Reset_Percent"]<-data[((data$Inferred==model_name)&(!(data$Reused=="N/A"))),"MQ_Reset"]/data[((data$Inferred==model_name)&((data$Reused=="N/A"))),"MQ_Reset"]
  #   data[((data$Inferred==model_name)&(!(data$Reused=="N/A"))),"Total_Resets_Percent"]<-data[((data$Inferred==model_name)&(!(data$Reused=="N/A"))),"Total_Resets"]/data[((data$Inferred==model_name)&((data$Reused=="N/A"))),"Total_Resets"]
  # }
  # data[(data$Reused=="N/A"),"EQ_Reset_Percent"]<-1
  # data[(data$Reused=="N/A"),"MQ_Reset_Percent"]<-1
  # data[(data$Reused=="N/A"),"Total_Resets_Percent"]<-1
  return(data)
}
calcCorrectness<-function(data,plotdir){
  
  tab_ok <- data
  tab_ok$Merged <- paste(tab_ok$Method,tab_ok$Success,sep = "|")
  tab_count<- rle(sort(tab_ok$Merged))
  
  
  df_ok <- data.frame(Method=tab_count$values, Total=tab_count$lengths)
  df_ok$Percent <- 0 
  df_ok[df_ok$Method=="DL*M_v1|OK", ]$Percent <- df_ok[df_ok$Method=="DL*M_v1|OK", ]$Total/sum(df_ok[df_ok$Method=="DL*M_v1|OK",]$Total,df_ok[df_ok$Method=="DL*M_v1|NOK",]$Total)
  df_ok[df_ok$Method=="DL*M_v1|NOK", ]$Percent <- df_ok[df_ok$Method=="DL*M_v1|NOK", ]$Total/sum(df_ok[df_ok$Method=="DL*M_v1|OK",]$Total,df_ok[df_ok$Method=="DL*M_v1|NOK",]$Total)
  
  df_ok[df_ok$Method=="DL*M_v2|OK", ]$Percent <- df_ok[df_ok$Method=="DL*M_v2|OK", ]$Total/sum(df_ok[df_ok$Method=="DL*M_v2|OK",]$Total,df_ok[df_ok$Method=="DL*M_v2|NOK",]$Total)
  df_ok[df_ok$Method=="DL*M_v2|NOK", ]$Percent <- df_ok[df_ok$Method=="DL*M_v2|NOK", ]$Total/sum(df_ok[df_ok$Method=="DL*M_v2|OK",]$Total,df_ok[df_ok$Method=="DL*M_v2|NOK",]$Total)
  
  df_ok[df_ok$Method=="LStarM|OK", ]$Percent <- df_ok[df_ok$Method=="LStarM|OK", ]$Total/sum(df_ok[df_ok$Method=="LStarM|OK",]$Total,df_ok[df_ok$Method=="LStarM|NOK",]$Total)
  df_ok[df_ok$Method=="LStarM|NOK", ]$Percent <- df_ok[df_ok$Method=="LStarM|NOK", ]$Total/sum(df_ok[df_ok$Method=="LStarM|OK",]$Total,df_ok[df_ok$Method=="LStarM|NOK",]$Total)
  
  df_ok$Percent <- round(100*df_ok$Percent,digits = 2)
  
  df_ok$Status<-"Failed"
  df_ok[grepl("|OK$",df_ok$Method), ]$Status<-"OK"
  df_ok$Method<-gsub("\\|N?OK$","",df_ok$Method)
  df_ok <- df_ok[,c(1,4,2,3)]
  
  p <- ggplot(df_ok, aes(x=Method, y=Percent) ) +
    geom_bar(aes(fill = Status),stat="identity") +
    scale_fill_manual(values = c("OK" = "light green", "Failed" = "red")) +
    scale_y_continuous(limits=c(0,100)) +
    labs(title = "Models correctly inferred (in %)", x = "Inference algorithm", y = "Percentage of correct hypotheses") +
    theme(
      plot.title = element_text(hjust = 0.5),
      plot.subtitle = element_text(hjust = 0.5),
      legend.position="right",
      axis.text.x = element_text(angle = 15, hjust = 1)
    )
  
  
  filename <- paste(plotdir,"/accuracy_",fname,out_format,sep = "");ggsave(filename, width = 7, height = 6,dpi=320)
  
  filename <- paste(plotdir,"/accuracy_",fname,".tab",sep = "")
  write.table(df_ok,filename,sep="\t",row.names=FALSE, quote=FALSE,dec=",",append=FALSE)
  return(df_ok)
}

calcEffectSize<-function(data){
  effsiz_methods <- character()
  effsiz_cntrl <- character()
  effsiz_treat <- character()
  effsiz_metr <- character()
  effsiz_wilc <- numeric()
  effsiz_vd <- numeric()
  effsiz_vd_mag <- character()
  
  effsiz_tab <- data.frame(effsiz_methods,
                           effsiz_cntrl,
                           effsiz_treat,
                           effsiz_metr,
                           effsiz_wilc,
                           effsiz_vd,effsiz_vd_mag)
  names(effsiz_tab) <- c("Methods","Control","Treatment","Metric",
                         "Wilcox",
                         "VD", "VD magnitude" )
  
  reused_lst  <- levels(data$Reused)
  reused_lst  <-reused_lst [! (reused_lst %in% c('N/A'))]
  
  tab_this<-data
  for(metric_id in c("MQ_Reset","EQ_Reset","Total_Resets","MQ_Symbol","EQ_Symbol")){
    for(sul in unique(data$Inferred)){
      for(reused in reused_lst){
        #####################################################
        control<-c(tab_this[((tab_this$Inferred==sul) & (tab_this$Reused=="N/A")),metric_id])
        treatment_v1<-c(tab_this[((tab_this$Inferred==sul) & (tab_this$Reused==reused) & (tab_this$Method=="DynFull")),metric_id])
        treatment_v2<-c(tab_this[((tab_this$Inferred==sul) & (tab_this$Reused==reused) & (tab_this$Method=="DynIncr")),metric_id])
        
        if((length(treatment_v1)==0) || (length(treatment_v2)==0)) next;
        
        #########################
        # L*M vs Dynamic L*M v1 #
        #########################
        wilc<-(wilcox.test(control, treatment_v1, conf.level = 0.95))        
        d <- (c(treatment_v1,control))
        f <- c(rep(c("Treatment"),each=length(treatment_v1)) , rep(c("Control"),each=length(control)))
        ## compute Vargha and Delaney
        effs_vd <- (VD.A(d,f))
        
        effsiz_tab <- rbind(effsiz_tab,data.frame(
          "Methods"= paste("L*M vs Dynamic L*M (v1)"),
          "Control"=sul,
          "Treatment"=reused,
          "Metric"=metric_id,
          "Wilcox"=(as.numeric(wilc[3])),
          "VD"=(effs_vd$estimate),
          "VD magnitude"=effs_vd$magnitude)
        )
        
        #########################
        # L*M vs Dynamic L*M v2 #
        #########################
        wilc<-(wilcox.test(control, treatment_v2, conf.level = 0.95))        
        d <- (c(treatment_v2,control))
        f <- c(rep(c("Treatment"),each=length(treatment_v2)) , rep(c("Control"),each=length(control)))
        ## compute Vargha and Delaney
        effs_vd <- (VD.A(d,f))
        
        effsiz_tab <- rbind(effsiz_tab,data.frame(
          "Methods"= paste("L*M vs Dynamic L*M (v2)"),
          "Control"=sul,
          "Treatment"=reused,
          "Metric"=metric_id,
          "Wilcox"=(as.numeric(wilc[3])),
          "VD"=(effs_vd$estimate),
          "VD magnitude"=effs_vd$magnitude)
        )
        
        #########################
        # Dynamic L*M v1 vs. v2 #
        #########################
        wilc<-(wilcox.test(treatment_v1, treatment_v2, conf.level = 0.95))        
        d <- (c(treatment_v2,treatment_v1))
        f <- c(rep(c("Treatment"),each=length(treatment_v2)) , rep(c("Control"),each=length(treatment_v1)))
        ## compute Vargha and Delaney
        effs_vd <- (VD.A(d,f))
        
        effsiz_tab <- rbind(effsiz_tab,data.frame(
          "Methods"= paste("Dynamic L*M (v1) vs Dynamic L*M (v2)"),
          "Control"=sul,
          "Treatment"=reused,
          "Metric"=metric_id,
          "Wilcox"=(as.numeric(wilc[3])),
          "VD"=(effs_vd$estimate),
          "VD magnitude"=effs_vd$magnitude)
        )
      }    
    }
  }
  
  
  rownames(effsiz_tab) <- NULL
  effsiz_tab$VD<-round(effsiz_tab$VD,digits = 3)
  effsiz_tab$Wilcox<-round(effsiz_tab$Wilcox,digits = 4)
  filename <- paste(plotdir,"/EffectSize.tab",sep="");
  write.table(effsiz_tab,filename,sep="\t",row.names=FALSE, quote=FALSE,dec=",",append=FALSE)
  return(effsiz_tab)
}

savePlot<-function(data,metric_id,plotdir){
  colourCount = length(unique(data$Inferred))
  coul = brewer.pal(12, "Paired") # My palette
  getPalette = colorRampPalette(coul)
  
  data_summ <- summarySE(data, measurevar=metric_id, groupvars=c("Method","Inferred", "Reused"))
  # data_summ[,paste(metric_id,"_Percent",sep = "")]<-0
  # for(sul in unique(data_summ$Inferred)){
  #   for(ruz in unique(data_summ[(data_summ$Inferred==sul),"Reused"])){
  #     val_tot<-data_summ[((data_summ$Inferred==sul)& (data_summ$Reused=="N/A")),metric_id]
  #     val_itm<-data_summ[((data_summ$Inferred==sul)& (data_summ$Reused==ruz)),metric_id]
  #     data_summ[((data_summ$Inferred==sul)& (data_summ$Reused==ruz)),paste(metric_id,"_Percent",sep = "")]<-val_itm/val_tot
  #   }
  # }
  # p2 <- ggplot(data=data_summ, aes_string(x="Inferred", y=metric_id, fill = "Reused")) +
  #   geom_bar(colour = "black", position='dodge', stat="identity", width=0.75) +
  #   geom_errorbar(aes(
  #     ymin=data_summ[,metric_id]-data_summ[,"ci"], ymax=data_summ[,metric_id]+data_summ[,"ci"]
  #   ), position = position_dodge(0.75),width = 0.2)+
  #   theme(plot.title = element_text(hjust = 0.5),legend.box.background = element_rect(),axis.text.x = element_text(angle = 45, hjust = 1))+
  #   labs(title = tab_filename)
  #   # scale_fill_manual(values = c("#A9A9A9",getPalette(colourCount)))
  # # scale_fill_brewer(palette="Spectral")
  # 
  # filename <- paste(plotdir,"/",metric_id,"_",fname,out_format,sep="");ggsave(filename, width = 15, height = 5,dpi=320)
  filename <- paste(plotdir,"/",metric_id,"_",fname,".tab",sep="");write.table(data_summ,filename,sep="\t",row.names=FALSE, quote=FALSE,dec=",",append=FALSE)
  
  
  bplots<-list()
  for(sul in unique(data$Inferred)){
    reused_models<-levels(factor(unique(data[(data$Inferred==sul),"Reused"])))
    reused_models<-gsub("^server_","srv_",gsub("^client_","cli_",reused_models))
    p2 <- ggplot(data=data[(data$Inferred==sul),], aes_string(x="Reused", y=metric_id,color="Method")) +
      # geom_boxplot(outlier.colour="red", outlier.shape=8, outlier.size=4)+
      geom_boxplot(outlier.shape=NA) +
      # scale_color_brewer(palette="Greys") +
      scale_color_grey() + theme_classic() + 
      scale_x_discrete(labels = reused_models)+
      theme(plot.title = element_text(hjust = 0.5,size=8),
            legend.position="none",
            axis.text.x = element_text(angle = 45, hjust = 1,size=7),
            axis.text.y = element_text(angle = 45, hjust = 1,size=7),
            axis.title.x = element_blank(), 
            axis.title.y = element_blank()
            ) +
      labs(title = gsub("^server_","srv_",gsub("^client_","cli_",sul)))+
      coord_cartesian(ylim=c(
        min(data_summ[(data_summ$Inferred==sul),metric_id]-data_summ[(data_summ$Inferred==sul),"sd"]), 
        max(data_summ[(data_summ$Inferred==sul),metric_id]+data_summ[(data_summ$Inferred==sul),"sd"])))
    bplots[[sul]]<-p2
  }
  bplots[[names(bplots)[1]]] <- bplots[[names(bplots)[1]]] + theme(axis.title.y = element_text(angle = 90,size=6))
  bplots[[names(bplots)[10]]] <- bplots[[names(bplots)[10]]] + theme(axis.title.y = element_text(angle = 90,size=6))
  p2<-plot_grid(plotlist=bplots,nrow = 2)
  p2<-plot_grid(p2,get_legend(bplots[[1]]+theme(legend.position = "bottom",legend.title=element_text(size=5),legend.text=element_text(size=5))),nrow = 2,rel_heights = c(1, .1))
  filename <- paste(plotdir,"/","boxplot_",metric_id,"_",fname,".pdf",sep="");
  ggsave(filename, width = 12, height = 4,dpi=320,title=paste(metric_id,"_boxplot_",fname,sep = ""))
  filename <- paste(plotdir,"/","boxplot_",metric_id,"_",fname,".png",sep="");
  ggsave(filename, width = 12, height = 4,dpi=320,title=paste(metric_id,"_boxplot_",fname,sep = ""))
}
args = commandArgs(trailingOnly=TRUE)
# out_format<-".png"
out_format<-".pdf"

# logdir<-args

# logdir<-"./experiment_verleg/Learning-SSH-Paper/models/"; fname<-"verleg"
# logdir<-"./experiment_usenix15/"; fname<-"usenix15_gnuTLS_server"
# logdir<-"./experiment_usenix15/"; fname<-"usenix15_gnuTLS_client"
# logdir<-"./experiment_usenix15/"; fname<-"usenix15_openSSL_srv"
# logdir<-"./experiment_usenix15/"; fname<-"usenix15_openSSL_cli"

logdir<-"/home/cdnd1/remote_euler/BenchmarkNordsec16/"; 

list.of.suls.to.remove <- c()
# # OpenSSL (server-side versions)
# fname<-"nordsec16_server2019_01_08_09_29_09_407"; side <-"server"
# list.of.suls.to.remove <- c(list.of.suls.to.remove
#   # ,"server_097","server_097c","server_097e","server_098l", "server_098m", "server_098s", "server_098u","server_098za", "server_101","server_101k", "server_102", "server_110-pre1", "server_100"  #remove
#   )
# tab_filename<-paste(logdir,fname,".tab",sep="")
# plotdir<- paste(logdir, "plots","/",fname,sep = "")
# data_srv<-loadTabAsDataFrame(tab_filename)
# 
# ## OpenSSL (client-side versions)
# fname<-"nordsec16_client2019_01_08_09_29_09_407"; side <-"client"
# list.of.suls.to.remove <- c(list.of.suls.to.remove,"client_097","client_097e","client_098f")
# #   "client_098j", "client_098l", "client_098m", "client_098za","client_101", "client_100m","client_101h","client_102","client_110-pre1"
# tab_filename<-paste(logdir,fname,".tab",sep="")
# plotdir<- paste(logdir, "plots","/",fname,sep = "")
# data_cli<-loadTabAsDataFrame(tab_filename)
# 
# data<-rbind(data_cli,data_srv)

fname<-"nordsec16_all"; side <-"all"
list.of.suls.to.remove <- c(list.of.suls.to.remove,"client_097","client_097e","client_098f")
#   "client_098j", "client_098l", "client_098m", "client_098za","client_101", "client_100m","client_101h","client_102","client_110-pre1"
tab_filename<-paste(logdir,fname,".tab",sep="")
plotdir<- paste(logdir, "plots","/",fname,sep = "")
data_all<-loadTabAsDataFrame(tab_filename)
data<-data_all

# logdir<-"./"; 
fname<-"nordsec16_all"; side <-"all"
plotdir<- paste(logdir, "plots","/",fname,sep = "")
dir.create(file.path(plotdir), showWarnings = FALSE,recursive = TRUE)

# data<-data_srv
# data<-data_cli
for(sulToRm in unique(data$Inferred)){
  if(length(unique(paste(data[((data$Inferred==sulToRm) & (!(data$Reused=="N/A"))),"Inferred"],data[((data$Inferred==sulToRm) & (!(data$Reused=="N/A"))),"Reused"])))==1){
    data <- data[(!((data$Inferred==sulToRm))),]
  }
}

calcCorrectness(data,plotdir)

data <-data[(data$Success=="OK"),]


#########################################################################
data_renamed<-data
data_renamed$Method<-gsub("LStarM","L*M",gsub("LStarM","L*M",gsub("DL.M_v1","DynFull",gsub("DL.M_v2","DynIncr",data_renamed$Method))))
data_renamed$Method<-factor(data_renamed$Method,levels = c("DynIncr","DynFull","L*M"))
metric_id<-"EQ_Reset"; savePlot(data_renamed,metric_id,plotdir)
metric_id<-"MQ_Reset"; savePlot(data_renamed,metric_id,plotdir)
metric_id<-"Rounds"; savePlot(data_renamed,metric_id,plotdir)
metric_id<-"Total_Resets"; savePlot(data_renamed,metric_id,plotdir)
effsiz_tab<-calcEffectSize(data_renamed)


# # metric_id<-"EQ_Symbol"; savePlot(data_renamed,metric_id,plotdir)
# # metric_id<-"MQ_Symbol"; savePlot(data_renamed,metric_id,plotdir)
# # metric_id<-"Total_Symbols"; savePlot(data_renamed,metric_id,plotdir)

# # mkAvgMeasurementsTexTab(data_renamed,effsiz_tab)
# # mkMwwEffSizeTexTabVert(data_renamed,effsiz_tab)
# # mkMwwEffSizeTexTabHoriz(data_renamed,effsiz_tab,sul_lst_ss,reused_lst_ss)

# data_summ <- data 
# 
# tab_filename<-paste(logdir,"releaseDatesSuls.tab",sep="")
# versions_info <- read.table(tab_filename, sep="\t", header=TRUE)
# versions_info$date<-as.Date(versions_info$date,format="%Y-%m-%d %H:%M:%S")
# # versions_info$version<-gsub("_","",gsub("^OpenSSL_","",versions_info$version))
# versions_info<-versions_info[order(versions_info$date),]
# versions_info$qsize<-as.numeric(versions_info$qsize)
# 
# ref_date <- min(versions_info$date)
# versions_info$day_number <- as.numeric(difftime(versions_info$date, ref_date))/(60*60*24)
# # versions_info$day_order <- seq(length(versions_info$date))
# 
# data_summ$Delta<-0
# 
# data_summ<-data_summ[(data_summ$Reused!="N/A"),]
# for(sul in unique(data_summ$Inferred)){
#   for(ruz in unique(data_summ[(data_summ$Inferred==sul),"Reused"])){
#     if(ruz!="N/A") {
#       if(is.null(data_summ[((data_summ$Inferred==sul)&(data_summ$Reused==ruz)),"Delta"])) skip
#       
#       # calculate delta(time)
#       delta_value<-versions_info[(versions_info$version==sul),"day_number"] - versions_info[(versions_info$version==ruz),"day_number"]
#       data_summ[((data_summ$Inferred==sul)&(data_summ$Reused==ruz)),"Delta"]<-delta_value
# 
#       # calculate  delta(qSize)
#       delta_qSize<-(versions_info[(versions_info$version==sul),"qsize"] - versions_info[(versions_info$version==ruz),"qsize"])
#       data_summ[((data_summ$Inferred==sul)&(data_summ$Reused==ruz)),"Delta_qSize"]<-delta_qSize
#       data_summ[((data_summ$Inferred==sul)&(data_summ$Reused==ruz)),"Delta_qSize_abs"]<-abs(delta_qSize)
#       
#     }
#   }
# }
#   
# 
# data_summ$MQ_Reset_NonReval<-data_summ$MQ_Reset-data_summ$MQ_Reset_Reval
# data_summ$MQ_Per_EQ<-data_summ$MQ_Reset/data_summ$EQ_Reset
# # data_summ_2<-data_summ[(data_summ$Delta_qSize>0),]
# # data_summ_2<-data_summ_2[(data_summ$Delta>0),]
# 
# # data_summ_2<-rbind(data_summ_2,data_summ)
# data_summ_2<-data_summ
# 
# # my_x = "Delta_qSize"; my_y = "MQ_Reset"; my_xlab = "Difference on the number of states"; my_ylab = "Number of MQs"
# my_x = "Delta"; my_y = "MQ_Reset"; my_xlab = "Distance between release dates in days"; my_ylab = "Number of MQs"
# ppp <-ggscatter(data_summ_2,
#           x = my_x,
#           y = my_y,
#           xlab = my_xlab,
#           ylab = my_ylab,
#           add = "reg.line",
#           cor.method = "pearson",
#           conf.int = TRUE, # Add confidence interval
#           cor.coef = TRUE # Add correlation coefficient. see ?stat_cor
#           
#           )
# filename <- paste(plotdir,"/PearsonCoef_",my_x,"_",my_y,".png",sep=""); ggsave(filename, width = 7, height = 5)
# filename <- paste(plotdir,"/PearsonCoef_",my_x,"_",my_y,".pdf",sep=""); ggsave(filename, width = 7, height = 5)
# 
# my_x = "Delta"; my_y = "EQ_Reset"; my_xlab = "Distance between release dates in days"; my_ylab = "Number of EQs"
# ppp <-ggscatter(data_summ_2,
#                 x = my_x,
#                 y = my_y,
#                 xlab = my_xlab,
#                 ylab = my_ylab,
#                 add = "reg.line",
#                 cor.method = "pearson",
#                 conf.int = TRUE, # Add confidence interval
#                 cor.coef = TRUE # Add correlation coefficient. see ?stat_cor
#                 
# )
# filename <- paste(plotdir,"/PearsonCoef_",my_x,"_",my_y,".png",sep=""); ggsave(filename, width = 7, height = 5)
# filename <- paste(plotdir,"/PearsonCoef_",my_x,"_",my_y,".pdf",sep=""); ggsave(filename, width = 7, height = 5)
# 
# 
# 
# # mkMwwEffSizeTexTabVert<-function(data,effsiz_tab){
# #   sul_lst<-levels(unique(effsiz_tab$Control)); sul_lst  <- sul_lst [! (sul_lst %in% list.of.suls.to.remove)]
# #   reused_lst<-levels(unique(effsiz_tab$Treatment)); reused_lst  <- reused_lst [! (reused_lst %in% list.of.suls.to.remove)]
# #   for(metric_id in c("MQ_Reset","EQ_Reset","Total_Resets","MQ_Symbol","EQ_Symbol","Weighted_Queries")){
# #     filename <- paste(plotdir,"/",metric_id,"_",fname,"_vert.tex.tab",sep="");
# #     data_summ <- summarySE(data, measurevar=metric_id, groupvars=c("Inferred", "Reused"))
# #     sink(filename)
# #     cat("SUL","Reused","p-value","Superior",paste("Effect size","\\\\ \\hline \n"),sep=" & ")
# #     for(sul in sul_lst){
# #       cat("\\multirow{",(length(data_summ[((data_summ$Inferred==sul)),metric_id])-1),"}{*}{",
# #           gsub("^server_","srv\\\\_",gsub("^client_","cli\\\\_",sul))
# #           ,"}",sep=" ")
# #       cat("\n")
# #       for(ruz in reused_lst){
# #         content_str<-paste(data_summ[((data_summ$Inferred==sul)&(data_summ$Reused==ruz)),metric_id],"",sep="")
# #         if(content_str!=""){
# #           reused_model <- gsub("^server_","srv\\\\_",gsub("^client_","cli\\\\_",ruz));
# #           avg_value <- data_summ[((data_summ$Inferred==sul)&(data_summ$Reused==ruz)),metric_id]
# #           p_value <- effsiz_tab[((effsiz_tab$Control==sul) & (effsiz_tab$Treatment==ruz)& (effsiz_tab$Metric==metric_id)),]$Wilcox
# #           eff_size <- effsiz_tab[((effsiz_tab$Control==sul) & (effsiz_tab$Treatment==ruz)& (effsiz_tab$Metric==metric_id)),]$VD
# #           superior<-"$\\mathtt{L^*_M}$"
# #           if(eff_size<0.5){
# #             superior<-"$\\mathtt{Dynamic~L^*_M}$"
# #           }
# #           eff_magn <- paste(effsiz_tab[((effsiz_tab$Control==sul) & (effsiz_tab$Treatment==ruz)& (effsiz_tab$Metric==metric_id)),]$VD.magnitude)
# #           sig_lv<-"";  
# #           if(p_value<0.01){
# #             sig_lv<-"**";
# #           }else if(p_value<0.05){
# #             sig_lv<-"*";
# #           }
# #           cat("",reused_model,
# #               # format(round(avg_value, 2), nsmall = 2),
# #               paste("$",format(round(p_value, 3), nsmall = 3),"^{~",sig_lv,"}$",sep = ""),
# #               superior,
# #               paste("$",
# #                     format(round(eff_size, 3), nsmall = 3),
# #                     "$ (",eff_magn,")\\\\ \\cline{2-5} \n",sep = ""),sep=" & ")
# #         }
# #       }
# #       cat("\\hline")
# #     }
# #     sink()
# #   }
# # }
# # mkMwwEffSizeTexTabHoriz<-function(data,effsiz_tab,to_consider){
# #   for(metric_id in c("MQ_Reset","EQ_Reset","Total_Resets","MQ_Symbol","EQ_Symbol","Weighted_Queries")){
# #     filename <- paste(plotdir,"/",metric_id,"_",fname,"_horiz.tex.tab",sep="");
# #     data_summ <- summarySE(data, measurevar=metric_id, groupvars=c("Inferred", "Reused"))
# #     sink(filename)
# #     cat("\\begin{tabular}{|c|")
# #     for(sul in sul_lst){
# #       for(ruz in reused_lst){
# #         content_str<-paste(data_summ[((data_summ$Inferred==sul)&(data_summ$Reused==ruz)),metric_id],"",sep="")
# #         if(content_str!=""){
# #           cat("c|")
# #         }
# #       }
# #     }
# #     cat("}\\hline\n")
# #     cat("SUL &")
# #     for(sul in sul_lst){
# #       cat(paste("\\multicolumn{",
# #             (length(data_summ[((data_summ$Inferred==sul)),metric_id])-1)
# #             ,"}{c|}{",gsub("_","\\\\_",sul),"} &",sep=" "))
# #     }
# #     cat("\\hline \n")
# #     for(sul in sul_lst){
# #       for(ruz in reused_lst){
# #         content_str<-paste(data_summ[((data_summ$Inferred==sul)&(data_summ$Reused==ruz)),metric_id],"",sep="")
# #         if(content_str!=""){
# #           cat(gsub("_","\\\\_",ruz),"& ")
# #         }
# #       }LL
# #     }
# #     cat("\\hline \n")
# #     cat("\\end{tabular}")
# #     sink()
# #   }
# # }
# # mkAvgMeasurementsTexTab<- function(data,effsiz_tab){
# #   sul_lst<-levels(unique(effsiz_tab$Control)); sul_lst  <- sul_lst [! (sul_lst %in% list.of.suls.to.remove)]
# #   reused_lst<-levels(unique(effsiz_tab$Treatment)); reused_lst  <- reused_lst [! (reused_lst %in% list.of.suls.to.remove)]
# #   for(metric_id in c("MQ_Reset","EQ_Reset","Total_Resets","MQ_Symbol","EQ_Symbol","Weighted_Queries")){
# #     filename <- paste(plotdir,"/",metric_id,"_",fname,".tex.tab",sep="");
# #     data_summ <- summarySE(data, measurevar=metric_id, groupvars=c("Inferred", "Reused"))
# #     sink(filename)
# #     cat("\\begin{tabular}{|c|c",rep("|l",(length(reused_lst)+1)),"|}",sep = "")
# #     cat("\n")
# #     cat("\\cline{3-",(length(reused_lst)+3),"}\n",sep = "")
# #     cat(
# #       "\\multicolumn{2}{c|}{ }",
# #       paste("\\multirow{2}{*}{","$L^*_M$","}",sep=" "), 
# #       paste("\\multicolumn{",(length(reused_lst)),"}{c|}{","Dynamic $L^*_M$","}",sep=" "), 
# #       sep = " & ")
# #     cat("\\\\ ",paste("\\cline{4-",(length(reused_lst)+3),"}",sep = "")," \n")
# #     cat("\\multicolumn{2}{c|}{ }","",gsub("^server_","srv\\\\_",gsub("^client_","cli\\\\_",reused_lst)),sep=" & ")
# #     cat("\\\\ \\hline \n")
# #     cat("\\multirow{",(length(sul_lst)),"}{*}{","SUL","}\n",sep=" ")
# #     for(sul in sul_lst){
# #       mylist<-c(gsub("^server_","srv\\\\_",gsub("^client_","cli\\\\_",sul)))
# #       
# #       number<-paste("${",round(data_summ[((data_summ$Inferred==sul)&(data_summ$Reused=="N/A")),metric_id],digits = 2),"}")
# #       
# #       sd<-"";
# #       if(data_summ[((data_summ$Inferred==sul)&(data_summ$Reused=="N/A")),]$sd!=0){
# #         sd<-paste(
# #           "_{\\pm ",
# #           round(data_summ[((data_summ$Inferred==sul)&(data_summ$Reused=="N/A")),]$sd,digits = 2),
# #           "}$",
# #           sep="")
# #       }
# #       mylist<-c(mylist,paste(number,sd,sep = " "))
# #       for(ruz in reused_lst){
# #         content_str<-paste(data_summ[((data_summ$Inferred==sul)&(data_summ$Reused==ruz)),metric_id],"",sep="")
# #         if(content_str==""){
# #           mylist<-c(mylist,'-')
# #         }else{
# #           number<-paste("{",round(data_summ[((data_summ$Inferred==sul)&(data_summ$Reused==ruz)),metric_id],digits = 2),"}")
# #           
# #           sd<-"";
# #           if(data_summ[((data_summ$Inferred==sul)&(data_summ$Reused==ruz)),]$sd!=0){
# #             sd<-paste(
# #               "_{\\pm ",
# #               round(data_summ[((data_summ$Inferred==sul)&(data_summ$Reused==ruz)),]$sd,digits = 2),
# #               "}",
# #               sep="")
# #           }
# #           
# #           content_str<-paste(
# #             "$",
# #             number,
# #             sd,
# #             "$",
# #             sep="")
# #           mylist<-c(mylist,content_str)
# #         }
# #         
# #       }
# #       cat("",mylist,sep=" & ")
# #       cat(" \\\\ ",
# #           paste("\\cline{2-",(length(reused_lst)+3),"}",sep = "")
# #           ," \n")
# #     }
# #     cat("\\hline")
# #     cat("\n")
# #     cat("\\end{tabular}")
# #     sink()
# #   } 
# # }