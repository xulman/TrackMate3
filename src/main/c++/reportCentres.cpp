#include <iostream>
#include <i3d/image3d.h>

template <typename VT>
bool reportCentre(const i3d::Image3d<VT>& img, const VT label,
                  i3d::Vector3d<float>& centre);


int main(int argc,char* argv[])
{
	//params:
	//manTraImg label[, label]

	std::cout << "image: " << argv[1] << "\n";
	i3d::Image3d<i3d::GRAY16> img(argv[1]);

	i3d::Vector3d<float> centre;
	std::cout.precision(1);
	int argI = 2;
	while (argI < argc)
	{
		int label = atol(argv[argI]);
		bool found = reportCentre(img,(i3d::GRAY16)label,centre);
		if (found)
			std::cout << "label " << label << " @ centre " << std::fixed
			          << centre.x << ","
			          << centre.y << ","
			          << centre.z << "\n";
		else
			std::cout << "label " << label << " @ centre --notFound--\n";
		++argI;
	}

	return 0;
}


template <typename VT>
bool reportCentre(const i3d::Image3d<VT>& img, const VT label,
                  i3d::Vector3d<float>& centre)
{
	centre = 0;
	long counter = 0;

	const VT* const p = img.GetFirstVoxelAddr();
	size_t i=0;

	while (i < img.GetImageSize())
	{
		if (p[i] == label)
		{
			centre.x += img.GetX(i);
			centre.y += img.GetY(i);
			centre.z += img.GetZ(i);
			++counter;
		}
		++i;
	}

	if (counter == 0) return false;

	centre /= (float)counter;
	return true;
}
