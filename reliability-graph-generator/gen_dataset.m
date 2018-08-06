% Scenario 1 - Parametro alpha basso - Delta discriminante
% Scenario 2 - Reliability utenti clique bassa - Delta discriminante (?)
% Scenario 3 - Reputation utenti clique bassa - Delta discriminante (?)

n=1000
nProducts=10000
gSizeMin=10
gSizeMax=20
nCliques=floor((0.9*n)/((gSizeMax+gSizeMin)/2))
nInterLinks=1
nOuterLinks=1

%nGroup=ceil(n/gsize)
%nGroupProd=ceil(nProducts/nCliques) % a number of ngroupprod "good" products per group
nProductsPerUserClique=5; %  the number of prefered products for each group will be len(groupProd)/n * nProducts * productRatio

nProductsOutsiderRatioRating=0.05; % percentage of (not prefered) products rated by each user with a high rating
nProductsOutsiderRatioStdDev=nProductsOutsiderRatioRating*0.1;

nProductsOutsiderRatioRating2=0.05; % percentage of products belonging to cliques rated by each user with a low rating
nProductsOutsiderRatioStdDev2=nProductsOutsiderRatioRating2*0.1;

nProductsLowRatingRatio=0.3;
nProductsLowRatingRatioStdDev=0.1*nProductsLowRatingRatio;

%% SCENARIOS %%

SC=1%
%SC=2
%SC=3

highestRate=6
middleRate=3
lowestRate=1

minRel=0
maxRel=0
minRelClique=0
maxRelClique=0

if(SC==1)
	minRel=0.2
	maxRel=0.8
	minRelClique=0.4
	maxRelClique=0.9
	minHlp=1
	midHlp=3
	maxHlp=6
elseif(SC==2)
	minRel=0.2
	maxRel=0.8
	minRelClique=0.1
	maxRelClique=0.4
	minHlp=1
	midHlp=3
	maxHlp=6
elseif(SC==3)	
	minRel=0.2
	maxRel=0.8
	minRelClique=0.4
	maxRelClique=0.9
	midHlp=3
	minHlp=1;
	maxHlp=midHlp;
else
	fprintf(stderr, "No valid scenario!"); 
endif

U=zeros(n,n);

global DBG=0
INTER=1

%% FUNCTION TO GENERATE CLIQUES %%
% nu = number of users
% nc = number of cliques
% cs = size of the clique
% will constructs nc cliques with no common elements
function [cliques, remainingsUsers]=buildCliques(nusers, nc, cs1, cs2)
	global DBG; 
	remainingsUsers=linspace(1,nusers,nusers); % vector [1, 2, 3, ... , nu]
	cliques={};
	ncreated=0;
	nu=cs1+floor((cs2-cs1)/2);
	sigma=max(1,floor(floor((cs2-cs1)/2)/4));
	printf("\r\n \r\n ** Generating %d cliques with size normally distributed [nu=%.2f, sigma=%.2f]", nc, nu, sigma);
	for i=1:nc
		cl=[];
		cs=min(max(cs1, floor(normrnd(nu, sigma))), cs2); 
		printf("\r\n\t ** Clique %d, size=%d", i, cs)
		for(j=1:cs)
			iu=floor(unifrnd(1,length(remainingsUsers)));
			u=remainingsUsers(iu);
			cl=[cl u];
			remainingsUsers(iu)=remainingsUsers(length(remainingsUsers));
			remainingsUsers(length(remainingsUsers))=[]; % pop extracted element
		endfor
		ncreated++;
		cliques{ncreated}=cl;
		if(DBG==1)
			printf("\r\n\t ** Clique %d, size=%d", i, cs); 
		endif
	endfor
endfunction 

%%MAIN %%

% nCliques of 
[allCliques, remainingsUsers] = buildCliques(n,nCliques,gSizeMin, gSizeMax);

printf("\r\n ** Size of remainings users: %d", length(remainingsUsers)); 

%Vector of groups (u->g), to write into a file 
groupVec=zeros(nCliques*gSizeMin,2);
totLinks=0;
printf("\r\n** Generating links within  %d groups/cliques", length(allCliques));

gvi=1;
for gi=1:length(allCliques)
	currentClique = allCliques{gi};

	%fill group gi into the vector of groups
	for iu=1:length(currentClique)
		groupVec(gvi,:)=[currentClique(iu),gi];
		gvi++; 
	endfor

	%1-Generate mutual links inside group gi
	printf("\r\n  ** Generating links for group %d..", gi);
	for i=1:length(currentClique)
		for j=1:length(currentClique)
			if(i!=j)
				u1 = currentClique(i);
				u2 = currentClique(j);
				assert(u1!=u2);
				U(u1,u2) = minRelClique + (maxRelClique-minRelClique)*rand(1);
				totLinks++;
				rel(totLinks,:)=[u1, u2, U(u1, u2)];
				if(DBG==1)
					printf("\r\n Added intra-group links, gr=%d, u1=%d, u2=%d, rel=%f", gi, rel(totLinks,1), rel(totLinks,2), rel(totLinks,3));
				endif
			endif
		endfor
	endfor
endfor


if(INTER==1)
	%Add random links between users of different groups/cliques 
	printf("\r\n ** Adding a number of inter-group link..");
	for gi=1:nCliques
		printf("\r\n\t ** Links from group %d..", gi);
		for gj=1:nCliques
			if(gi!=gj) % select two users at random
				if(mod(gj,20)==0)
					printf("\r\n --> %d .. ", gj);
				endif
				for k=1:nInterLinks
					iu1=floor(unifrnd(1, length(allCliques{gi})));
					iu2=floor(unifrnd(1, length(allCliques{gj})));
					u1=allCliques{gi}(iu1);
					u2=allCliques{gj}(iu2);
					assert(u1!=u2); 
					if(U(u1, u2)==0)
						U(u1, u2) = minRel + (maxRel-minRel)*rand(1);
						totLinks++;
						rel(totLinks, :) = [u1, u2, U(u1, u2)];
						if(DBG==1)
							printf("\r\n Added inter-group link %d-->%d, rel=%f", rel(totLinks,1), rel(totLinks,2), rel(totLinks,3));
						endif %DBG
					endif % link u1-> u2 does not exist or u1==u2
				endfor % no. of inter-links
			endif % gi!-gj 
		endfor % for all gj 
	endfor % for all gi

	%Add random links between cliques and remaining users 
	l=length(remainingsUsers);
	remainingsUsersCopy = remainingsUsers; 
	printf("\n\n ** Adding %d links between outsiders users and cliques, outsiders=%d, nCliques=%d", nOuterLinks*length(remainingsUsers), length(remainingsUsers), nCliques);
	for iu=1:l 
		u1=remainingsUsers(1); % for all remained users 
		remainingsUsers(1)=remainingsUsers(length(remainingsUsers));
		remainingsUsers(length(remainingsUsers))=[]; % pop 
		gi=floor(unifrnd(1,nCliques)); % random clique 
		clique=allCliques{gi}; 
		iug=floor(unifrnd(1,length(clique))); % random user of the clique gi
		ug=clique(iug);
		%link u1-->ug
		assert(u1!=ug);
		if(U(u1, ug)==0)
			U(u1, ug)=minRel + (maxRel-minRel)*rand(1);
			totLinks++;
			rel(totLinks, :)=[u1, ug, U(u1, ug)];
			if(DBG==1)
				printf("\n Added user-to-clique link %d-->%d, rel=%f", rel(totLinks,1), rel(totLinks,2), rel(totLinks,3));
			endif
		else
			printf("\r\n WARN: Link %d-->%d already exists", u1, ug);
		endif
	endfor
endif

printf("\n"); 

% ggroups.txt and greliability.txt 
dlmwrite("ggroups.txt", groupVec, "delimiter", "\t", "newline", "\r\n", "precision", "%.2f");
dlmwrite("greliability.txt", rel, "delimiter", "\t", "newline", "\r\n", "precision", "%.2f");

nnz=length(find(U))

%fill array nGroupProd, it will contain, at index i (i=index of clique) the number of prefered 
%products of the group/clique i
nGroupProd=zeros(length(allCliques));
nGroupProdVec={}; 
remainingProds=linspace(1, nProducts, nProducts);
printf("** Generating prefered products for %d cliques ", length(allCliques));
for i=1:length(nGroupProd)
	nGroupProd(i)=floor(length(allCliques{i}) * nProductsPerUserClique);
	cliqueProds=[];
	printf("** \r\n Clique %d, number of prefered products=%d", i, nGroupProd(i));
	if(DBG==1)
		printf("** \r\n Clique %d, list of products: [\r\n", i); 
	endif
	for j=1:nGroupProd(i)
		ip=floor(unifrnd(1, length(remainingProds))); 
		p=remainingProds(ip); 
		cliqueProds=[cliqueProds p]; % add product to vector of products  
		remainingProds(ip)=remainingProds(length(remainingProds)); % swap current with last 
		remainingProds(length(remainingProds))=[]; % pop products from the list
		if(DBG==1)
			printf(" %d ",p );
		endif
	endfor
	if(DBG==1)
		printf("\r\n ]\r\n ");
	endif
	nGroupProdVec{i}=cliqueProds; % store vector of prefered products for ith clique
endfor

totRating=0;
cat=4; %the category is not relevant
%hlp=3; %helfuness --> reputation

for gi=1:nCliques 
	clique=allCliques{gi}; % vector of users
	%for each user  of the clique, a certain number of products are assigned and evaluated with high score 
	nProdClique = nGroupProd(gi); % number of product to rate well
	
	for iu=1:length(clique)
		user=clique(iu); 
		printf("\r\n\r\n ** User=%d, Sampling high rating for %d products.. ", user, nProdClique);
		cliqueProds = nGroupProdVec{gi}; % list of prefered products for the clique 
		for j=1:nProdClique %for each product in the set of good product for the user group
			totRating++;
			r=floor(unifrnd(middleRate+1,highestRate)); %uniform distribution
			hlp=floor(unifrnd(minHlp,maxHlp));
			p=cliqueProds(j); 
			if(DBG==1)
				printf("\r\nUser=%d, group=%d, p=%d, cat=%d, rating=%d, helpfulness=%d", user, gi, p, cat, r, hlp);
			endif
			rating(totRating,:)=[user, p, cat, r, hlp]; % to generate ``good'' rate for the product
		endfor % end GOOD rating 
		cc=0;

		%Sample a number of products not prefered by any clique/group (remainings products)
		remainingProdsCopy = remainingProds;
		numRating = min(floor(1.0*nProdClique), length(remainingProdsCopy));  
		for i=1:numRating
			%gj=floor(unifrnd(1,nCliques));
			pi = floor(unifrnd(1,length(remainingProdsCopy))); % a product selected randomly 
			p=remainingProdsCopy(pi);
			remainingProdsCopy(ip) = remainingProdsCopy(length(remainingProdsCopy));
			remainingProdsCopy(length(remainingProdsCopy)) = []; % pop product

			hlp=floor(unifrnd(minHlp, maxHlp)); % average reputation
			r=middleRate; % middleRate
			if(DBG==1)
				printf("\r\nUser =%d,clique=%d, p=%d, cat=%d, rating=%d, helpfulness=%d",user,gi, p, cat, r, hlp);
			endif
			totRating++; 
			rating(totRating,:)=[user, p, cat, r, hlp]; 
		endfor
		printf("\r\n\t ** User=%d, Sampled %d products (low rate) not prefered by any clique **\n", user, numRating);
	endfor % end for each user of the clique
endfor % end for each clique

numOutsiderRatingMean = floor(nProductsOutsiderRatioRating*length(remainingProds));
numOutsiderRatingStdDev = max(1, floor(nProductsOutsiderRatioStdDev*length(remainingProds)));
printf("\r\n ** Number of rating for outsiders (not beloging to a clique) and products not prefered for any clique, mean=%.2f, stdDev=%.2f", numOutsiderRatingMean, numOutsiderRatingStdDev);

numOutsiderRatingMean2 = floor(nProductsOutsiderRatioRating2*length(nProducts-remainingProds));
numOutsiderRatingStdDev2 = max(1, floor(nProductsOutsiderRatioStdDev2*length(nProducts-remainingProds)));
printf("\r\n ** Number of rating for outsiders (beloging to a clique) and products recommended by groups, mean=%.2f, stdDev=%.2f", numOutsiderRatingMean2, numOutsiderRatingStdDev2);


%% RATING OF USERS THAT DO NOT BELONG TO ANY GROUP %%

printf("** \r\n Number of outsiders: %d", length(remainingsUsersCopy)); 

%numRemainings=floor(unifrnd(1,length(remainingsUsersCopy)));
numRemainings=length(remainingsUsersCopy); 

for i=1:numRemainings
	u=remainingsUsersCopy(i);
	numRating = floor(normrnd(numOutsiderRatingMean, numOutsiderRatingStdDev));
	remainingProdsCopy = remainingProds; 
	printf("\r\n ** User=%d, Trying to sample %d products (high rate) not in any clique and user not in any clique (available products=%d)  **", u, numRating, length(remainingProdsCopy));
	for i=1:numRating
		%gj=floor(unifrnd(1,nCliques));
		pi = floor(unifrnd(1,length(remainingProdsCopy))); % a product selected randomly from a random clique
		p=remainingProdsCopy(pi);
		remainingProdsCopy(ip) = remainingProdsCopy(length(remainingProdsCopy));
		remainingProdsCopy(length(remainingProdsCopy)) = []; % pop product

		hlp=floor(unifrnd(midHlp, maxHlp)); % average reputation 
		r=floor(unifrnd(middleRate+1, highestRate)); % high rating 
		if(DBG==1)
			printf("\r\nUser=%d, p=%d, cat=%d, rating=%d, helpfulness=%d", u, p, cat, r, hlp);
		endif
		totRating++; 
		rating(totRating,:)=[u, p, cat, r, hlp]; 
	endfor
	printf("\r\n\t ** User=%d, Sampled %d products **\n", u, numRating);

	
	numRating = max(5, floor(normrnd(numOutsiderRatingMean2, numOutsiderRatingStdDev2)));
% Now we sample some products beloning to the cliques at low rate
printf("\r\n ** User=%d, Trying to sample %d products (low rate) in products recommended by the cliques **", u, numRating);

	for i=1:numRating
		%select a random clique 
		gr_random = floor(unifrnd(1,nCliques)); 
		% products to recommend at low rating
		prodClique = nGroupProdVec{gr_random};	

		pi = floor(unifrnd(1,length(prodClique))); % a product selected randomly from a random clique
		p=prodClique(pi);
		
		hlp=floor(unifrnd(midHlp, maxHlp)); % high reputation 
		r=floor(unifrnd(lowestRate, middleRate)); % low rating value 
		if(DBG==1)
			printf("\r\nUser=%d, p=%d, cat=%d, rating=%d, helpfulness=%d", u, p, cat, r, hlp);
		endif
		totRating++; 
		rating(totRating,:)=[u, p, cat, r, hlp]; 
	endfor
	printf("\r\n\t ** User=%d, Sampled %d products (recommended by groups) at low rating from user not in any group **\n", u, numRating);
endfor

dlmwrite("grating.txt", rating, "delimiter", "\t", "newline", "\r\n");
