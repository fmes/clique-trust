a=load("test.txt"); 
s=statistics(a(:,2))(1:5,:)';

printf("%.4f %.4f %.4f %.4f %.4f", s(1), s(2),  s(3), s(4),  s(5)); 
